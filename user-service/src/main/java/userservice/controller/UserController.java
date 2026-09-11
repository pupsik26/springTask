package userservice.controller;

import userservice.dto.UserDto;
import userservice.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.hateoas.CollectionModel;
import org.springframework.hateoas.EntityModel;
import org.springframework.hateoas.server.mvc.WebMvcLinkBuilder;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
@Tag(name = "Users", description = "API для управления пользователями")
public class UserController {

    private final UserService userService;

    @GetMapping
    @Operation(summary = "Получить всех пользователей")
    public ResponseEntity<CollectionModel<EntityModel<UserDto>>> getAll() {
        List<EntityModel<UserDto>> users = userService.findAll().stream()
                .map(this::addLinks)
                .collect(Collectors.toList());

        CollectionModel<EntityModel<UserDto>> collectionModel = CollectionModel.of(users);
        collectionModel.add(
                WebMvcLinkBuilder.linkTo(WebMvcLinkBuilder.methodOn(UserController.class).getAll())
                        .withSelfRel()
        );

        return ResponseEntity.ok(collectionModel);
    }

    @GetMapping("/{id}")
    @Operation(summary = "Получить пользователя по ID")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Пользователь найден"),
            @ApiResponse(responseCode = "404", description = "Пользователь не найден")
    })
    public ResponseEntity<EntityModel<UserDto>> getById(
            @Parameter(description = "ID пользователя") @PathVariable Long id) {
        UserDto user = userService.findById(id);
        return ResponseEntity.ok(addLinks(user));
    }

    @PostMapping
    @Operation(summary = "Создать нового пользователя")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Пользователь создан"),
            @ApiResponse(responseCode = "400", description = "Невалидные данные")
    })
    public ResponseEntity<EntityModel<UserDto>> create(@Valid @RequestBody UserDto dto) {
        UserDto created = userService.create(dto);
        return ResponseEntity.status(HttpStatus.CREATED).body(addLinks(created));
    }

    @PutMapping("/{id}")
    @Operation(summary = "Обновить пользователя")
    public ResponseEntity<EntityModel<UserDto>> update(
            @PathVariable Long id,
            @Valid @RequestBody UserDto dto) {
        UserDto updated = userService.update(id, dto);
        return ResponseEntity.ok(addLinks(updated));
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Удалить пользователя")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "204", description = "Пользователь удален"),
            @ApiResponse(responseCode = "404", description = "Пользователь не найден")
    })
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        userService.delete(id);
        return ResponseEntity.noContent().build();
    }

    private EntityModel<UserDto> addLinks(UserDto user) {
        EntityModel<UserDto> resource = EntityModel.of(user);

        resource.add(
                WebMvcLinkBuilder.linkTo(WebMvcLinkBuilder.methodOn(UserController.class)
                                .getById(user.getId()))
                        .withSelfRel()
        );

        resource.add(
                WebMvcLinkBuilder.linkTo(WebMvcLinkBuilder.methodOn(UserController.class)
                                .getAll())
                        .withRel("users")
        );

        return resource;
    }
}