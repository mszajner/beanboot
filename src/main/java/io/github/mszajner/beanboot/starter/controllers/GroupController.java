package io.github.mszajner.beanboot.starter.controllers;

import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import io.github.mszajner.beanboot.starter.api.Group;
import io.github.mszajner.beanboot.starter.api.GroupCreate;
import io.github.mszajner.beanboot.starter.api.GroupService;

import java.util.List;
import java.util.Set;
import java.util.UUID;

@RestController
@RequestMapping("/api/groups")
@RequiredArgsConstructor
@Tag(name = "Groups")
public class GroupController {

    private final GroupService groupService;

    @GetMapping
    public List<Group> getGroups() {
        return groupService.getGroups();
    }

    @GetMapping("/{id}")
    public Group getGroup(@PathVariable UUID id) {
        return groupService.getGroup(id);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public Group createGroup(@RequestBody GroupCreate groupCreate) {
        return groupService.createGroup(groupCreate);
    }

    @PutMapping("/{id}")
    public Group updateGroup(@PathVariable UUID id, @RequestBody Group group) {
        return groupService.updateGroup(id, group);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteGroup(@PathVariable UUID id) {
        groupService.deleteGroup(id);
    }

    @PutMapping("/{groupId}/users")
    public Group setGroupUsers(@PathVariable UUID groupId, @RequestBody Set<UUID> userIds) {
        return groupService.setGroupUsers(groupId, userIds);
    }
}
