package com.github.wkennedy.repositories;

import com.github.wkennedy.entities.Person;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;
import reactor.core.Disposable;
import reactor.core.publisher.Flux;

import java.util.concurrent.CountDownLatch;
import java.util.stream.IntStream;
import java.util.stream.Stream;

@RunWith(SpringRunner.class)
@SpringBootTest
public class PersonReactiveMongoRepositoryTest {

    private static final Logger log = LoggerFactory.getLogger(PersonReactiveMongoRepositoryTest.class);

    @Autowired
    private PersonReactiveMongoRepository personReactiveMongoRepository;

    @Before
    public void setUp() throws Exception {
        personReactiveMongoRepository.deleteAll().block();
        createPersons().then().block();
    }

    @Test
    public void findAllTest() throws Exception {
        CountDownLatch latch = new CountDownLatch(1);
        Disposable subscription = personReactiveMongoRepository.findAll()
                .doOnNext(System.out::println)
                .doOnComplete(latch::countDown)
                .doOnError(throwable -> latch.countDown())
                .subscribe();
        latch.await();
        subscription.dispose();
    }

    private Flux<Person> createPersons() {
        Stream<Person> personStream = IntStream.range(0, 10)
                .parallel()
                .mapToObj(i -> new Person("John", "Doe" + i))
                .peek(person -> log.info("Creating person: " + person.toString()));

        return personReactiveMongoRepository.save(Flux.fromStream(personStream));
    }

}