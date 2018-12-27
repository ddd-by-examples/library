package io.pillopl.books.domain;

import static io.pillopl.books.domain.Patron.PatronType.REGULAR;
import static io.pillopl.books.domain.Patron.PatronType.RESEARCHER;

class PatronFixture {

    static Patron regularPatron() {
        return new Patron(OverdueResources.noOverdueResources(), REGULAR, 0);
    }

    static Patron researcherPatron() {
        return new Patron(OverdueResources.noOverdueResources(), RESEARCHER, 0);
    }

    static Patron regularPatronWithHolds(int numberOfHolds) {
        return new Patron(OverdueResources.noOverdueResources(), REGULAR, numberOfHolds);
    }

    static Patron researcherPatronWithHolds(int numberOfHolds) {
        return new Patron(OverdueResources.noOverdueResources(), RESEARCHER, numberOfHolds);
    }

    static Patron regularPatronWithOverdueResource(OverdueResources overdueResources) {
        return new Patron(overdueResources, REGULAR, 0);
    }

    static Patron researcherPatronWithOverdueResource(OverdueResources overdueResources) {
        return new Patron(overdueResources, RESEARCHER, 0);
    }
}
