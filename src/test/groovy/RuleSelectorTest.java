import org.junit.Assert;
import org.junit.Test;

import java.util.List;

public class RuleSelectorTest {
    Location testLocation = Starter.getTestLocation();

    @Test
    public void singleQueryWithoutPredictions() {
        List<District> districts = RuleSelectorFactory.selector(testLocation, District.class)
                .query("cities.districts").toList();
        Assert.assertEquals(3, districts.size());
    }

    @Test
    public void singleQueryWithPredictions() {
        List<District> districts = RuleSelectorFactory.selector(testLocation, District.class)
                .query("cities.districts", district -> district.getName().endsWith("1") || district.getName().endsWith("3")).toList();
        Assert.assertEquals(2, districts.size());
    }

    @Test
    public void multipleQueryWithPredictions() {
        List<Street> streets = RuleSelectorFactory.selector(testLocation, District.class)
                .query("cities.districts", district -> district.getName().endsWith("1") || district.getName().endsWith("3"))
                .toSelector(District.class, Street.class)
                .query("roads.streets")
                .toList();
        Assert.assertEquals(18, streets.size());
    }

    @Test
    public void multipleQueryWithMultiplePredictions() {
        List<Street> streets = RuleSelectorFactory.selector(testLocation, District.class)
                .query("cities.districts", district -> district.getName().endsWith("1") || district.getName().endsWith("3"))
                .toSelector(District.class, Street.class)
                .query("roads.streets", street -> street.getName().endsWith("3"))
                .toList();
        Assert.assertEquals(3, streets.size());

        List<Road> roads = RuleSelectorFactory.selector(testLocation, District.class)
                .query("cities.districts", district -> district.getName().endsWith("1") || district.getName().endsWith("3"))
                .toSelector(District.class, Street.class)
                .query("roads.streets", street -> street.getName().endsWith("3"))
                .toSelector(Road.class, Road.class)
                .toList();
        Assert.assertEquals(3, roads.size());
    }
}
