package de.acosci.tasks.controller.rest;

import de.acosci.tasks.model.entity.User;
import de.acosci.tasks.service.impl.UserService;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("api/users")
@RequiredArgsConstructor
@CrossOrigin(origins = {"http://localhost", "http://localhost:5173/", "http://localhost:3000/"})
public class UserRestController {
    @Autowired
    private final UserService userService;

    @GetMapping
    public ResponseEntity<List<User>> getAllUsers() {
        try {
            return new ResponseEntity<>(userService.getUsers(), HttpStatus.OK);
        } catch (EntityNotFoundException e) {
            return ResponseEntity.badRequest().build();
        }
    }

    @GetMapping("/{id}")
    public ResponseEntity<User> getUserById(@PathVariable long id) {
        try {
            return new ResponseEntity<User>(userService.getUserByID(id), HttpStatus.OK);
        } catch (EntityNotFoundException e) {
            return ResponseEntity.notFound().build();
        }
    }

    @PostMapping
    public ResponseEntity<User> createUser(@RequestBody User user) {
        try {
            return new ResponseEntity<User>(userService.saveUser(user), HttpStatus.CREATED);
        } catch(Exception e) { // todo
            return ResponseEntity.badRequest().build();
        }
    }



    @PutMapping
    public ResponseEntity<User> updateUserByID(@RequestBody User user) {
        try {
            return new ResponseEntity<User>(userService.saveUser(user), HttpStatus.OK);
        } catch(Exception e) { // todo
            return ResponseEntity.badRequest().build();
        }
    }

    @DeleteMapping("/{id}")
    public ResponseEntity deleteTaskByID(@PathVariable Long id) {
        try {
            userService.deleteUserByID(id);
            return new ResponseEntity<>(HttpStatus.OK);
        } catch(Exception e) { // todo
            return ResponseEntity.badRequest().build();
        }
    }

    @DeleteMapping
    public ResponseEntity deleteUser(@RequestBody User user) {
        try {
            userService.deleteUser(user);
            return new ResponseEntity<>(HttpStatus.OK);
        } catch(Exception e) { // todo
            return ResponseEntity.badRequest().build();
        }
    }
}
