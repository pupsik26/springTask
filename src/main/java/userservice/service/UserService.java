package userservice.service;

import org.springframework.kafka.core.KafkaTemplate;
import userservice.dto.UserDto;
import userservice.dto.UserEventDto;
import userservice.entity.User;
import userservice.exception.UserNotFoundException;
import userservice.mapper.UserMapper;
import userservice.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;
    private final UserMapper userMapper; // Внедряем маппер

    private final KafkaTemplate<String, UserEventDto> kafkaTemplate; // <-- Добавили

    private static final String TOPIC_NAME = "user-events";

    public List<UserDto> findAll() {
        return userRepository.findAll().stream()
                .map(userMapper::toDto)
                .toList();
    }

    public UserDto findById(Long id) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new UserNotFoundException(id));
        return userMapper.toDto(user);
    }

    @Transactional
    public UserDto create(UserDto dto) {
        User user = userMapper.toEntity(dto);
        User saved = userRepository.save(user);

        kafkaTemplate.send(TOPIC_NAME, new UserEventDto("CREATE", saved.getEmail()));

        return userMapper.toDto(saved);
    }

    @Transactional
    public UserDto update(Long id, UserDto dto) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new UserNotFoundException(id));

        user.setName(dto.getName());
        user.setEmail(dto.getEmail());
        user.setAge(dto.getAge());

        User saved = userRepository.save(user);
        return userMapper.toDto(saved);
    }

    @Transactional
    public void delete(Long id) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new UserNotFoundException(id));

        kafkaTemplate.send(TOPIC_NAME, new UserEventDto("DELETE", user.getEmail()));

        userRepository.deleteById(id);
    }
}