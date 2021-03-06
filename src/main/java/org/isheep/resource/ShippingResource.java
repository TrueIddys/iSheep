package org.isheep.resource;

import org.isheep.config.javax.validation.groups.JacksonGroup;
import org.isheep.config.security.CurrentCustomer;
import org.isheep.entity.Customer;
import org.isheep.entity.Shipping;
import org.isheep.entity.Tracking;
import org.isheep.repository.ShippingRepository;
import org.isheep.service.ParcelPriceCalculator;
import org.joda.time.DateTime;
import org.springframework.http.HttpStatus;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import sun.util.resources.es.CalendarData_es;

import javax.inject.Inject;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/**
 * Created by raymo on 21/11/2016.
 */
@RequestMapping(ShippingResource.BASE_URL)
@RestController
public class ShippingResource {
    public static final String BASE_URL = "/shipping";

    private final ShippingRepository shippingRepository;
    private final ParcelPriceCalculator calculator;

    @Inject
    public ShippingResource(final ShippingRepository shippingRepository, final ParcelPriceCalculator calculator) {
        this.shippingRepository = shippingRepository;
        this.calculator = calculator;
    }

    @ResponseStatus(HttpStatus.OK)
    @RequestMapping(value = "/me", method = RequestMethod.GET)
    public final List<Shipping> myShippings(@CurrentCustomer final Customer customer) {
        return shippingRepository.findBySender(customer);
    }

    @ResponseStatus(HttpStatus.CREATED)
    @RequestMapping(method = RequestMethod.POST)
    public final Shipping create(@CurrentCustomer final Customer customer, @Validated(JacksonGroup.class) @RequestBody final Shipping shipping) {
        if (shipping.getId() != null) {
            throw new IllegalArgumentException("Cannot persist an entity if ID is already defined");
        }

        shipping.setSender(customer);
        shipping.setPrice(calculator.calculateShippingPrice(shipping));

        final List<Tracking> trackings = new ArrayList<>();

        final DateTime dateEvent = DateTime.now();
        trackings.add(new Tracking(dateEvent.toDate(), "Votre colis est arrivé à la station de trie de Paris"));
        trackings.add(new Tracking(dateEvent.plusMinutes(3).toDate(), "Votre colis est arrivé à la station de trie de Lyon"));
        trackings.add(new Tracking(dateEvent.plusMinutes(8).toDate(), "Votre colis est arrivé à la station de trie de Clermont-Ferrand"));

        shipping.setTrackings(trackings);

        return shippingRepository.save(shipping);
    }

}
