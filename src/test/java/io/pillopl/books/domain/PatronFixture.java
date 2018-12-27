package io.pillopl.books.domain;

import static io.pillopl.books.domain.Patron.PatronType.REGULAR;
import static io.pillopl.books.domain.Patron.PatronType.RESEARCHER;

class PatronFixture {

    static Patron regularPatron() {
        return new Patron(anyPatronId(), OverdueResources.noOverdueResources(), REGULAR, 0);
    }
    static Patron regularPatron(String patronId) {
        return new Patron(patronId(patronId), OverdueResources.noOverdueResources(), REGULAR, 0);
    }

    static Patron researcherPatron() {
        return new Patron(anyPatronId(), OverdueResources.noOverdueResources(), RESEARCHER, 0);
    }

    static Patron regularPatronWithHolds(int numberOfHolds) {
        return new Patron(anyPatronId(), OverdueResources.noOverdueResources(), REGULAR, numberOfHolds);
    }

    static Patron researcherPatronWithHolds(int numberOfHolds) {
        return new Patron(anyPatronId(), OverdueResources.noOverdueResources(), RESEARCHER, numberOfHolds);
    }

    static Patron regularPatronWithOverdueResource(OverdueResources overdueResources) {
        return new Patron(anyPatronId(), overdueResources, REGULAR, 0);
    }

    static Patron researcherPatronWithOverdueResource(OverdueResources overdueResources) {
        return new Patron(anyPatronId(), overdueResources, RESEARCHER, 0);
    }

    static PatronId anyPatronId() {
        return patronId("Patron");
    }


    static PatronId patronId(String patronId) {
        return new PatronId(patronId);
    }


}
