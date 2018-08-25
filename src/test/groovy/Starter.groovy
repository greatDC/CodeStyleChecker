import com.fasterxml.jackson.core.JsonGenerator
import com.fasterxml.jackson.core.JsonParser
import com.fasterxml.jackson.dataformat.xml.XmlMapper
import groovy.transform.PackageScope

import java.time.LocalDateTime
import java.util.function.BiFunction
import java.util.function.Predicate

class Starter {


    static void main(String[] args) {
        Location location = getTestLocation()

        String methodName = "printTest"
        "${methodName}"(location)
//        println(LocalDateTime.now())
//        println new JsonBuilder(scriptGroovyInJava('location', 'location.cities.districts.roads.flatten().find{ it.streets.any{it.name == "Street3ax"} }', location)).toPrettyString()
//        println(LocalDateTime.now())
//        println new JsonBuilder(scriptGroovyInJava('location', 'location.cities.districts.flatten().find{ it.roads.streets.flatten().any{it.name == "Street3bz"} }', location)).toPrettyString()
//        println(LocalDateTime.now())
//        new JsonBuilder(scriptGroovyInJava('location', '[location.cities.districts].flatten().find{ [it.roads.streets.name].flatten().contains("Street1ax") }', location)).toPrettyString()
//        println(LocalDateTime.now())
//        new JsonBuilder(scriptGroovyInJava('location', '[location.cities.districts].flatten().find{ [it.roads.streets.name].flatten().contains("Street2ay") }', location)).toPrettyString()
//        println(LocalDateTime.now())
//        new JsonBuilder(scriptGroovyInJava('location', '[location.cities.districts].flatten().find{ [it.roads.streets.name].flatten().contains("Street3az") }', location)).toPrettyString()
//        println(LocalDateTime.now())
//
//        def selectParam = /cities.districts/
//        def fromParam = location
//        def whereParam = {district -> district.roads.streets.name.flatten().contains("Street3az")}
//        def district1 = select selectParam from fromParam where whereParam
//        println(district1)
//        def district2 = select 'cities.districts' from location where {district -> district.roads.streets.name.flatten().contains("Street3az")}
//        println(district2)
//        assert new JsonBuilder(district1).toPrettyString() == new JsonBuilder(district2).toPrettyString()
//        println(new JsonBuilder(district2).toPrettyString())

        RuleSelectorFactory.selector(location, Street.class).query("cities/districts/roads/streets", null)
        // location/cities/districts/roads[filters]/streets/communities
    }

    public static Location getTestLocation() {
        XmlMapper xmlMapper = new XmlMapper();
        def filePath = '/Users/renzhengwei/Workstation/Workspace/git/CodeStyleChecker/src/test/groovy/location.xml'
        xmlMapper.configure(JsonParser.Feature.IGNORE_UNDEFINED, true)
        xmlMapper.configure(JsonGenerator.Feature.IGNORE_UNKNOWN, true)
        Location location = xmlMapper.readValue(new File(filePath).readLines().join("\n"), Location)
        location
    }

    static void printTest(Location location) {
        println(location)
        println LocalDateTime.now()
    }

    static def select(String fieldChain) {
        [from: { theListOrElementOfPojoType ->
            [where: { criteria ->
                List pojoList = [theListOrElementOfPojoType]
                fieldChain.split('[.]').each {
                    pojoList = [pojoList][it]
                }
                pojoList = pojoList.flatten()
                pojoList.findAll criteria
            }]
        }]
    }

    static def scriptGroovyInJava(varname, expression, obj) {

        def binding = new Binding()
        def shell = new GroovyShell(binding)
        binding.setVariable(varname, obj)
//        binding.setVariable('y',3)
        shell.evaluate "z = ${expression}"
        binding.getVariable('z')
    }
}

class BaseLocation {
    String name
}

class BaseLocationWithMajor<T> extends BaseLocation {
    T first
}

class Location extends BaseLocationWithMajor<City> {
    List<City> cities
}

class City extends BaseLocationWithMajor<District> {
    List<District> districts
}

class District extends BaseLocationWithMajor<Road> {
    List<Road> roads
}

class Road extends BaseLocationWithMajor<Street> {
    List<Street> streets
}

class Street extends BaseLocationWithMajor<Community> {
    List<Community> communities
}

class Community extends BaseLocationWithMajor<Building> {
    List<Building> buildings
}

class Building extends BaseLocationWithMajor<Unit> {
    List<Unit> units
}

class Unit extends BaseLocationWithMajor<Floor> {
    List<Floor> floors
}

class Floor extends BaseLocationWithMajor<Room> {
    List<Room> rooms
}

class Room extends BaseLocation {
}

class RuleSelectorBase {
    boolean retired
    List mutableRootNodeList
    Map<Object, Object> mapFromChildToParent = [:]

    public <T> List<T> toList() {
        retired = true
        mutableRootNodeList
    }
}

class RuleSelectorFactory {
    static <T> RuleSelector<T> selector(Object sourceNode, Class<T> type) {
        new RuleSelector(sourceNode, type)
    }
}

class RuleSelector<T> extends RuleSelectorBase {

    Class<T> predictingType

    @PackageScope
    RuleSelector(Object sourceNode, Class<T> predictingType) {
        this.mutableRootNodeList = [sourceNode].flatten()
        this.predictingType = predictingType
    }

    RuleSelectorQuery query(String pattern, Predicate<T> predicate = { T it -> true }) {
        if (retired) {
            throw new RuntimeException("RETIRED")
        }
        pattern.split(/[.]/).each { field ->
            List parentNodeList = mutableRootNodeList.flatten()
            mutableRootNodeList = []
            parentNodeList.each { parentNode ->
                Object currentNode = parentNode[field]
                if (!currentNode) {
                    return
                }
                if (currentNode instanceof List) {
                    currentNode.findAll {
                        it.getClass() != predictingType || predicate.test(it)
                    }.each {
                        mutableRootNodeList << it
                        mapFromChildToParent.put(it, parentNode)
                    }
                } else if (currentNode.getClass() != predictingType || predicate.test(currentNode)) {
                    mutableRootNodeList << currentNode
                    mapFromChildToParent.put(currentNode, parentNode)
                }
            }
        }
        cleanUnreachedParentNodes()
        retired = true
        new RuleSelectorQuery(mutableRootNodeList: mutableRootNodeList.flatten(), mapFromChildToParent: mapFromChildToParent)
    }

    private void cleanUnreachedParentNodes() {
        List validNodeList = []
        mutableRootNodeList.each { leafNode ->
            Object validNode = leafNode
            validNodeList << validNode

            Object nextValidNode = mapFromChildToParent.get(validNode)
            while (nextValidNode) {
                validNodeList << nextValidNode
                nextValidNode = mapFromChildToParent.get(nextValidNode)
            }
        }
        mapFromChildToParent.entrySet().removeAll { !validNodeList.contains(it.key) }
    }
}

class RuleSelectorQuery extends RuleSelectorBase {

    public <T> RuleSelector<T> toSelector(Class<?> sourceNodeType, Class<T> predictionType, Class<T> parentType = Void.class) {
        mutableRootNodeList = []
        mapFromChildToParent.each {
            Object childNode = it.key
            if (childNode.getClass() == sourceNodeType) {
                mutableRootNodeList << childNode
            }
            Object parentNode = it.value
            if (parentNode.getClass() == sourceNodeType) {
                mutableRootNodeList << parentNode
            }
        }
        mutableRootNodeList = mutableRootNodeList.unique()
        // TODO: check if there are multiple parent nodes from different layers
        mapFromChildToParent.clear()
        new RuleSelector(mutableRootNodeList, predictionType)
    }
}
