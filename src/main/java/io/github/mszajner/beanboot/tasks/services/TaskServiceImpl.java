package io.github.mszajner.beanboot.tasks.services;

import io.github.mszajner.beanboot.tasks.api.*;
import jakarta.annotation.security.PermitAll;
import lombok.RequiredArgsConstructor;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.transaction.annotation.Transactional;
import io.github.mszajner.beanboot.tasks.api.*;
import io.github.mszajner.beanboot.tasks.entities.TaskEntity;
import io.github.mszajner.beanboot.tasks.mappers.TaskMapper;
import io.github.mszajner.beanboot.tasks.repositories.TaskRepository;

import java.util.Map;
import java.util.Objects;

@RequiredArgsConstructor
public class TaskServiceImpl implements TaskService {

    private final TaskRepository taskRepository;
    private final TaskMapper taskMapper;
    private final ApplicationEventPublisher eventPublisher;

    @Override
    @Transactional(readOnly = true)
    @PermitAll
    public Page<Task> getTasks(int page, int size, String q, String objectId, TaskObjectType objectType, TaskAction action, TaskStatus status) {
        var pageable = PageRequest.of(page, Math.min(size, 100), Sort.by(Sort.Direction.DESC, "createdAt"));
        var spec = buildSpec(q, objectId, objectType, action, status);
        return taskRepository.findAll(spec, pageable)
                .map(taskMapper::toDto);
    }

    private Specification<TaskEntity> buildSpec(String q, String objectId, TaskObjectType objectType, TaskAction action, TaskStatus status) {
        Specification<TaskEntity> spec = (root, query, cb) -> cb.conjunction();
        if (q != null) {
            var pattern = "%" + q.toLowerCase() + "%";
            spec = spec.and((root, query, cb) -> cb.or(
                    cb.like(cb.lower(root.get("message")), pattern),
                    cb.like(cb.lower(root.get("objectName")), pattern),
                    cb.like(cb.lower(root.get("parameters")), pattern)
            ));
        }
        if (objectId != null) {
            spec = spec.and((root, query, cb) -> cb.equal(root.get("objectId"), objectId));
        }
        if (objectType != null) {
            spec = spec.and((root, query, cb) -> cb.equal(root.get("objectType"), objectType));
        }
        if (action != null) {
            spec = spec.and((root, query, cb) -> cb.equal(root.get("action"), action));
        }
        if (status != null) {
            spec = spec.and((root, query, cb) -> cb.equal(root.get("status"), status));
        }
        return spec;
    }

    @Override
    @Transactional
    public Task createTask(TaskAction action) {
        return createTask(action, null, null);
    }

    @Override
    @Transactional
    public Task createTask(TaskAction action, TaskableObject object) {
        return createTask(action, object, null);
    }

    @Override
    @Transactional
    public Task createTask(TaskAction action, TaskableObject object, Map<String, String> parameters) {
        var task = new TaskEntity();
        task.setAction(action);
        task.setStatus(TaskStatus.PENDING);
        task.setParameters(parameters);
        if (Objects.nonNull(object)) {
            task.setObjectId(object.getTaskObjectId());
            task.setObjectName(object.getTaskObjectName());
            task.setObjectType(object.getTaskObjectType());
        }
        var saved = taskRepository.save(task);
        eventPublisher.publishEvent(new TaskCreatedEvent());
        return taskMapper.toDto(saved);
    }
}
