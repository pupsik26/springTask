package userservice.service;

import userservice.dto.UserDto;
import userservice.dto.UserEventDto;
import userservice.entity.User;
import userservice.exception.UserNotFoundException;
import userservice.mapper.UserMapper;
import userservice.repository.UserRepository;
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class UserService {

    private final UserRepository userRepository;
    private final UserMapper userMapper;
    private final KafkaTemplate<String, UserEventDto> kafkaTemplate;

    private static final String TOPIC = "user-events";
    private static final String CB_NAME = "kafkaCircuitBreaker";

    public List<UserDto> findAll() {
        return userRepository.findAll().stream().map(userMapper::toDto).toList();
    }

    public UserDto findById(Long id) {
        return userMapper.toDto(userRepository.findById(id)
                .orElseThrow(() -> new UserNotFoundException(id)));
    }

    @Transactional
    @CircuitBreaker(name = CB_NAME, fallbackMethod = "createFallback")
    public UserDto create(UserDto dto) {
        User saved = userRepository.save(userMapper.toEntity(dto));
        sendKafkaEvent(new UserEventDto("CREATE", saved.getEmail()));
        return userMapper.toDto(saved);
    }

    // Fallback-метод — вызывается, когда circuit breaker OPEN
    public UserDto createFallback(UserDto dto, Throwable t) {
        log.warn("Circuit breaker сработал при создании пользователя: {}. " +
                "Пользователь сохранён в БД, но событие в Kafka не отправлено.", dto.getEmail());
        return userMapper.toDto(
                userRepository.findByEmail(dto.getEmail()).orElse(null)
        );
    }

    @Transactional
    public UserDto update(Long id, UserDto dto) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new UserNotFoundException(id));
        user.setName(dto.getName());
        user.setEmail(dto.getEmail());
        user.setAge(dto.getAge());
        return userMapper.toDto(userRepository.save(user));
    }

    @Transactional
    @CircuitBreaker(name = CB_NAME, fallbackMethod = "deleteFallback")
    public void delete(Long id) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new UserNotFoundException(id));
        sendKafkaEvent(new UserEventDto("DELETE", user.getEmail()));
        userRepository.deleteById(id);
    }

    public void deleteFallback(Long id, Throwable t) {
        log.warn("Circuit breaker сработал при удалении пользователя с id={}. " +
                "Пользователь НЕ удалён из БД.", id);
        throw new RuntimeException("Не удалось удалить пользователя: сервис уведомлений недоступен", t);
    }

    private void sendKafkaEvent(UserEventDto event) {
        kafkaTemplate.send(TOPIC, event);
        log.info("Событие отправлено в Kafka: {}", event);
    }
}