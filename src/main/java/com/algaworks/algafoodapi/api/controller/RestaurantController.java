package com.algaworks.algafoodapi.api.controller;


import com.algaworks.algafoodapi.domain.exception.EntityBeingUsedException;
import com.algaworks.algafoodapi.domain.exception.EntityNotFoundException;
import com.algaworks.algafoodapi.domain.model.Kitchen;
import com.algaworks.algafoodapi.domain.model.Restaurant;
import com.algaworks.algafoodapi.domain.repository.RestaurantRepository;
import com.algaworks.algafoodapi.domain.service.RestaurantRegisterService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.util.ReflectionUtils;
import org.springframework.web.bind.annotation.*;

import java.lang.reflect.Field;
import java.net.URI;
import java.util.List;
import java.util.Map;
import java.util.Objects;

@RestController
@RequestMapping("/restaurants")
public class RestaurantController {

    @Autowired
    RestaurantRepository restaurantRepository;

    @Autowired
    RestaurantRegisterService restaurantRegisterService;



    @GetMapping
    public List<Restaurant> list(){
        return restaurantRepository.list();
    }

    @GetMapping("/{restaurantId}")
    public ResponseEntity<Restaurant> search(@PathVariable Long restaurantId){
        Restaurant restaurant = restaurantRepository.search(restaurantId);

        if (restaurant != null) {
            return ResponseEntity.ok(restaurant);
        }
        return ResponseEntity.notFound().build();
    }



    @PostMapping
    public ResponseEntity<?> add(@RequestBody Restaurant restaurant) {
        try {
            restaurant = restaurantRegisterService.save(restaurant);

            return ResponseEntity.status(HttpStatus.CREATED).body(restaurant);
//            return ResponseEntity.created(URI.create("/" + restaurant.getId())).build();

        } catch (EntityNotFoundException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }


    @PutMapping("/{restaurantId}")
    public ResponseEntity<?> update(@PathVariable Long restaurantId,
                                    @RequestBody Restaurant restaurant) {
        try {
            Restaurant currentRestaurant = restaurantRepository.search(restaurantId);

            if (currentRestaurant != null) {
                BeanUtils.copyProperties(restaurant, currentRestaurant, "id");
                currentRestaurant = restaurantRegisterService.save(currentRestaurant);

                return ResponseEntity.ok(currentRestaurant);
            }
            return ResponseEntity.notFound().build();

        } catch (EntityNotFoundException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }


    @PatchMapping("/{restaurantId}")
    public ResponseEntity<?> updatePartially(@PathVariable Long restaurantId,
                                                      @RequestBody Map<String, Object> fields) {

        Restaurant currentRestaurant = restaurantRepository.search(restaurantId);

        if( currentRestaurant == null) {
            return ResponseEntity.notFound().build();
        }

        merge(fields, currentRestaurant);

        return update(restaurantId, currentRestaurant);

    }


    private void merge(Map<String, Object> sourceFields, Restaurant targetRestaurant) {
        ObjectMapper objectMapper = new ObjectMapper();
        Restaurant sourceRestaurant = objectMapper.convertValue(sourceFields, Restaurant.class);

        sourceFields.forEach((propertyName, propertyValue) -> {
            Field field = ReflectionUtils.findField(Restaurant.class, propertyName);
            field.setAccessible(true);

            Object newValue = ReflectionUtils.getField(field, sourceRestaurant);

            ReflectionUtils.setField(field, targetRestaurant, newValue);
        });

    }



    @DeleteMapping("/{restaurantId}")
    public ResponseEntity<Restaurant> delete(@PathVariable Long restaurantId) {
        try {
            restaurantRegisterService.delete(restaurantId);
            return ResponseEntity.noContent().build();

        } catch (EntityNotFoundException e){
            return ResponseEntity.notFound().build();

        } catch (EntityBeingUsedException e){
            return ResponseEntity.status(HttpStatus.CONFLICT).build();
        }
    }
}
