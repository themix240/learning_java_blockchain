package search;

import org.junit.jupiter.api.Test;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

class SearchEngineTest {
    @Test
    void searchTest() {
        People john = new People("John", "Doe", "johndoe@mail.com");
           People jan = new People("Jan", "Kowalski", "jk@gmail.com");
        People[] people = {john,jan};
        SearchEngine se = new SearchEngine(people);
        Optional<People> test = se.Search("Jan", "").stream().findFirst();
        assertTrue(test.isPresent());
        assertEquals(jan, test.get());
        Optional<People> test2 = se.Search("Adam", "").stream().findFirst();
        assertFalse(test2.isPresent());
    }
}