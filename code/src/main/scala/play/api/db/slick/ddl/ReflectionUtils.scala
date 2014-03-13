package play.api.db.slick.ddl

import org.reflections.scanners
import org.reflections.util
import org.reflections.Reflections
import scala.reflect.runtime.universe
import scala.reflect.runtime.universe._

object ReflectionUtils {
  import annotation.tailrec

  def getReflections(classloader: ClassLoader, pkg: String): Option[Reflections] = {
    val scanUrls = org.reflections.util.ClasspathHelper.forPackage(pkg, classloader)
    if (!scanUrls.isEmpty)
      Some(new Reflections(new util.ConfigurationBuilder()
        .addUrls(scanUrls)
        .filterInputsBy(new util.FilterBuilder().include(util.FilterBuilder.prefix(pkg + ".")))
        .setScanners(new scanners.TypeAnnotationsScanner, new scanners.TypesScanner)))
    else
      None
  }

  def splitIdentifiers(names: String) = names.split("""\.""").filter(!_.trim.isEmpty).toList
  def assembleIdentifiers(ids: List[String]) = ids.mkString(".")

  def findFirstModule(names: String)(implicit mirror: JavaMirror): Option[ModuleSymbol] = {
    val elems = splitIdentifiers(names)
    var i = 1 //FIXME: vars...
    var res: Option[ModuleSymbol] = None
    while (i < (elems.size + 1) && !res.isDefined) {
      try {
        val symbol = mirror.staticModule(assembleIdentifiers(elems.slice(0, i)))
        mirror.reflectModule(symbol).instance //if we can reflect a module it means we are module 
        res = Some(symbol)
      } catch {
        case _: reflect.internal.MissingRequirementError =>
        //FIXME: must be another way to check if a static modules exists than exceptions!?!
        case _: ClassNotFoundException =>
        //We tried to reflect a module but got a class cast exception (again, would be nice to do this differently)
      } finally {
        i += 1
      }
    }
    res
  }

  def reflectModuleOrField(name: String, base: Any, baseSymbol: Symbol)(implicit mirror: JavaMirror) = {
    val baseIM = mirror.reflect(base)
    val baseMember = baseSymbol.typeSignature.member(newTermName(name))
    val instance = if (baseMember.isModule) {
      if (baseMember.isStatic) {
        mirror.reflectModule(baseMember.asModule).instance
      } else {
        baseIM.reflectModule(baseMember.asModule).instance
      }
    } else {
      assert(baseMember.isTerm, "Expected " + baseMember + " to be something that can be reflected on " + base + " as a field")
      baseIM.reflectField(baseMember.asTerm).get
    }
    instance -> baseMember
  }

  def scanModuleOrFieldByReflection(instance: Any, sym: Symbol)(checkSymbol: Symbol => Boolean)(implicit mirror: JavaMirror): List[(Any, Symbol)] = {
    @tailrec def scanModuleOrFieldByReflection(found: List[(Any, Symbol)],
      checked: Vector[Symbol],
      instancesNsyms: List[(Any, Symbol)]): List[(Any, Symbol)] = {

      val extractMembers: PartialFunction[(Any, Symbol), Iterable[(Any, Symbol)]] = {
        case (baseInstance, baseSym) =>
          if (baseInstance != null) {
            val vals = baseSym.typeSignature.members.filter(s => s.isModule || (s.isTerm && s.asTerm.isVal))
            vals.flatMap { mSym =>
              try {
                List(reflectModuleOrField(mSym.name.decoded, baseInstance, baseSym))
              } catch {
                case _: ScalaReflectionException => List.empty
              }
            }
          } else List.empty
      }
      val matching = instancesNsyms.flatMap(extractMembers).filter { case (_, s) => checkSymbol(s) }
      val candidates = instancesNsyms.flatMap(extractMembers).filter { case (_, s) => !checkSymbol(s) && !checked.contains(s) }
      if (candidates.isEmpty)
        (found ++ matching).distinct
      else
        scanModuleOrFieldByReflection(found ++ matching, checked ++ (matching ++ candidates).map(_._2), candidates)
    }

    scanModuleOrFieldByReflection(List.empty, Vector.empty, List(instance -> sym))
  }

}
