package search;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.Test;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

class InvertedIndexSearchEngineTest {

    @Test
    void searchTest() {
        People john = new People("John", "Doe", "johndoe@mail.com");
        People[] people = {john};
        SearchEngine se =new SearchEngine(people);
        InvertedIndexSearchEngine tested = new InvertedIndexSearchEngine(se);
        Optional<People> result = tested.Search("Jan John Abc","ANY").stream().findFirst();
        assertTrue(result.isPresent());
        assertEquals(john,result.get());
        Optional<People> result2 = tested.Search("John","NONE").stream().findFirst();
        assertFalse(result2.isPresent());
        Optional<People> result3 = tested.Search("John Doe","ALL").stream().findFirst();
        assertTrue(result3.isPresent());
        assertEquals(john,result3.get());
        Optional<People> result4 = tested.Search("John Dou","ALL").stream().findFirst();
        assertFalse(result4.isPresent());
    }
}