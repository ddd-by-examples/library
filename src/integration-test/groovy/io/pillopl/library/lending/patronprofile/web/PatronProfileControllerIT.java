package io.pillopl.library.lending.patronprofile.web;

import io.pillopl.library.catalogue.BookId;
import io.pillopl.library.lending.LendingTestContext;
import io.pillopl.library.lending.book.model.BookFixture;
import io.pillopl.library.lending.patron.application.hold.CancelingHold;
import io.pillopl.library.lending.patron.model.PatronFixture;
import io.pillopl.library.lending.patron.model.PatronId;
import io.pillopl.library.lending.patronprofile.model.CheckoutsView;
import io.pillopl.library.lending.patronprofile.model.HoldsView;
import io.pillopl.library.lending.patronprofile.model.PatronProfile;
import io.pillopl.library.lending.patronprofile.model.PatronProfiles;
import io.vavr.Tuple;
import io.vavr.control.Try;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.hateoas.MediaTypes;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.servlet.MockMvc;

import java.time.Instant;
import java.util.UUID;

import static io.pillopl.library.commons.commands.Result.Success;
import static io.vavr.collection.List.of;
import static java.time.Instant.now;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.is;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.springframework.http.HttpHeaders.CONTENT_TYPE;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@RunWith(SpringRunner.class)
@WebMvcTest(PatronProfileController.class)
@ContextConfiguration(classes = {LendingTestContext.class})
public class PatronProfileControllerIT {

    PatronId patronId = PatronFixture.anyPatronId();
    BookId bookId = BookFixture.anyBookId();
    BookId anotherBook = BookFixture.anyBookId();

    Instant anyDate = now();
    Instant anotherDate = now().plusSeconds(100);


    @Autowired
    private MockMvc mvc;

    @MockBean
    private PatronProfiles patronProfiles;

    @MockBean
    private CancelingHold cancelingHold;

    @Test
    public void shouldContainPatronProfileResourceWithCorrectHeadersAndLinksToCheckoutsAndHolds() throws Exception {
        given(patronProfiles.fetchFor(patronId)).willReturn(profiles());

        //expect
        mvc.perform(get("/profiles/" + patronId.getPatronId())
                .accept(MediaTypes.HAL_FORMS_JSON_VALUE))
                .andExpect(status().isOk())
                .andExpect(header().string(CONTENT_TYPE, MediaTypes.HAL_FORMS_JSON_VALUE + ";charset=UTF-8"))
                .andExpect(jsonPath("$._links.self.href", containsString("profiles/" + patronId.getPatronId())))
                .andExpect(jsonPath("$.patronId", is(patronId.getPatronId().toString())))
                .andExpect(jsonPath("$._links.holds.href", containsString("/profiles/" + patronId.getPatronId() + "/holds")))
                .andExpect(jsonPath("$._links.checkouts.href", containsString("/profiles/" + patronId.getPatronId() + "/checkouts")));
    }

    @Test
    public void shouldCreateLinksForHolds() throws Exception {
        given(patronProfiles.fetchFor(patronId)).willReturn(profiles());

        //expect
        mvc.perform(get("/profiles/" + patronId.getPatronId() + "/holds/")
                .accept(MediaTypes.HAL_FORMS_JSON_VALUE))
                .andExpect(status().isOk())
                .andExpect(header().string(CONTENT_TYPE, MediaTypes.HAL_FORMS_JSON_VALUE + ";charset=UTF-8"))
                .andExpect(jsonPath("$._embedded.holdList[0].bookId", is(bookId.getBookId().toString())))
                .andExpect(jsonPath("$._embedded.holdList[0]._links.self.href", containsString("/profiles/" + patronId.getPatronId() + "/holds/" + bookId.getBookId())))
                .andExpect(jsonPath("$._embedded.holdList[0].till", is(anyDate.toString())))
                .andExpect(jsonPath("$._embedded.holdList[0]._templates.default.method", is("delete")));
    }

    @Test
    public void shouldCreateLinksForCheckouts() throws Exception {
        given(patronProfiles.fetchFor(patronId)).willReturn(profiles());

        //expect
        mvc.perform(get("/profiles/" + patronId.getPatronId() + "/checkouts/")
                .accept(MediaTypes.HAL_FORMS_JSON_VALUE))
                .andExpect(status().isOk())
                .andExpect(header().string(CONTENT_TYPE, MediaTypes.HAL_FORMS_JSON_VALUE + ";charset=UTF-8"))
                .andExpect(jsonPath("$._embedded.checkoutList[0].bookId", is(anotherBook.getBookId().toString())))
                .andExpect(jsonPath("$._embedded.checkoutList[0].till", is(anotherDate.toString())))
                .andExpect(jsonPath("$._embedded.checkoutList[0]._links.self.href", containsString("/profiles/" + patronId.getPatronId() + "/checkouts/" + anotherBook.getBookId())));

    }

    @Test
    public void shouldReturn404WhenThereIsNoHold() throws Exception {
        given(patronProfiles.fetchFor(patronId)).willReturn(profiles());

        //expect
        mvc.perform(get("/profiles/" + patronId.getPatronId() + "/holds/" + UUID.randomUUID())
                .accept(MediaTypes.HAL_FORMS_JSON_VALUE))
                .andExpect(status().isNotFound());
    }

    @Test
    public void shouldReturn404WhenThereIsNoCheckout() throws Exception {
        given(patronProfiles.fetchFor(patronId)).willReturn(profiles());

        //expect
        mvc.perform(get("/profiles/" + patronId.getPatronId() + "/checkouts/" + UUID.randomUUID())
                .accept(MediaTypes.HAL_FORMS_JSON_VALUE))
                .andExpect(status().isNotFound());
    }

    @Test
    public void shouldReturnResourceForHold() throws Exception {
        given(patronProfiles.fetchFor(patronId)).willReturn(profiles());

        //expect
        mvc.perform(get("/profiles/" + patronId.getPatronId() + "/holds/" + bookId.getBookId())
                .accept(MediaTypes.HAL_FORMS_JSON_VALUE))
                .andExpect(status().isOk())
                .andExpect(header().string(CONTENT_TYPE, MediaTypes.HAL_FORMS_JSON_VALUE + ";charset=UTF-8"))
                .andExpect(jsonPath("$.bookId", is(bookId.getBookId().toString())))
                .andExpect(jsonPath("$.till", is(anyDate.toString())))
                .andExpect(jsonPath("$._templates.default.method", is("delete")))
                .andExpect(jsonPath("$._links.self.href", containsString("profiles/" + patronId.getPatronId() + "/holds/" + bookId.getBookId())));
    }

    @Test
    public void shouldReturnResourceForCheckout() throws Exception {
        given(patronProfiles.fetchFor(patronId)).willReturn(profiles());

        //expect
        mvc.perform(get("/profiles/" + patronId.getPatronId() + "/checkouts/" + anotherBook.getBookId())
                .accept(MediaTypes.HAL_FORMS_JSON_VALUE))
                .andExpect(status().isOk())
                .andExpect(header().string(CONTENT_TYPE, MediaTypes.HAL_FORMS_JSON_VALUE + ";charset=UTF-8"))
                .andExpect(jsonPath("$.bookId", is(anotherBook.getBookId().toString())))
                .andExpect(jsonPath("$.till", is(anotherDate.toString())))
                .andExpect(jsonPath("$._links.self.href", containsString("profiles/" + patronId.getPatronId() + "/checkouts/" + anotherBook.getBookId())));
    }

    @Test
    public void shouldCancelExistingHold() throws Exception {
        given(patronProfiles.fetchFor(patronId)).willReturn(profiles());
        given(cancelingHold.cancelHold(any())).willReturn(Try.success(Success));

        //expect
        mvc.perform(delete("/profiles/" + patronId.getPatronId() + "/holds/" + bookId.getBookId())
                .accept(MediaTypes.HAL_FORMS_JSON_VALUE))
                .andExpect(status().isNoContent());
    }

    @Test
    public void shouldNotCancelNotExistingHold() throws Exception {
        given(patronProfiles.fetchFor(patronId)).willReturn(profiles());
        given(cancelingHold.cancelHold(any())).willReturn(Try.failure(new IllegalArgumentException()));

        //expect
        mvc.perform(delete("/profiles/" + patronId.getPatronId() + "/holds/" + bookId.getBookId())
                .accept(MediaTypes.HAL_FORMS_JSON_VALUE))
                .andExpect(status().isNotFound());
    }

    @Test
    public void shouldReturn500IfSomethingFailedWhileCanceling() throws Exception {
        given(patronProfiles.fetchFor(patronId)).willReturn(profiles());
        given(cancelingHold.cancelHold(any())).willReturn(Try.failure(new IllegalStateException()));

        //expect
        mvc.perform(delete("/profiles/" + patronId.getPatronId() + "/holds/" + bookId.getBookId())
                .accept(MediaTypes.HAL_FORMS_JSON_VALUE))
                .andExpect(status().is(500));
    }


    PatronProfile profiles() {
        return new PatronProfile(
                new HoldsView(of(Tuple.of(bookId, anyDate))),
                new CheckoutsView(of(Tuple.of(anotherBook, anotherDate))));
    }
}