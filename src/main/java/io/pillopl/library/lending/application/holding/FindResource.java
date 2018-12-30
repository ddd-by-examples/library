package io.pillopl.library.lending.application.holding;


import io.pillopl.library.lending.domain.resource.Resource;
import io.pillopl.library.lending.domain.resource.ResourceId;
import io.vavr.control.Option;

@FunctionalInterface
interface FindResource {

    Option<Resource> with(ResourceId resourceId);
}
