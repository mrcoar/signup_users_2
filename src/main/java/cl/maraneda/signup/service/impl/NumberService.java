package cl.maraneda.signup.service.impl;

import cl.maraneda.signup.dto.NumberDTO;
import cl.maraneda.signup.dto.UserDTO;
import cl.maraneda.signup.model.Number;
import cl.maraneda.signup.model.User;
import cl.maraneda.signup.repository.NumberRepository;
import cl.maraneda.signup.service.INumberService;

import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

@Service
public class NumberService implements INumberService {
    private transient final NumberRepository repository;
    private transient final ModelMapper mapper;

    @Autowired
    public NumberService(NumberRepository nr, ModelMapper mm){
        repository = nr;
        mapper = mm;
    }

    @Override
    public void addAll(List<NumberDTO> numbersDTO, UserDTO udto) {
        List<Number> numbers=new ArrayList<>();
        IntStream.range(0, numbersDTO.size()).forEach(i ->
            numbers.add(Number.builder()
                    .phoneNumber(numbersDTO.get(i).getPhoneNumber())
                    .cityCode(numbersDTO.get(i).getCityCode())
                    .countryCode(numbersDTO.get(i).getCountryCode())
                    .user(mapper.map(udto, User.class))
                    .build())
        );
        repository.saveAll(numbers);
    }

    public int countByUserId(String userId){
        return repository.countByUserId(userId);
    }

    @Override
    public List<NumberDTO> getByUser(String userId) {
        List<Number> numbers = repository.findByUserId(userId);
        if (numbers == null) {
            return Collections.emptyList(); // or throw an exception, depending on requirements
        }
        return numbers.stream()
                .filter(Objects::nonNull)
                .map(number -> NumberDTO.builder()
                        .cityCode(number.getCityCode())
                        .countryCode(number.getCountryCode())
                        .phoneNumber(number.getPhoneNumber())
                        .build())
                .collect(Collectors.toList());

    }

    public void deleteByUserEmailPrefix(String prefix){
        repository.deleteByUserEmailPrefix(prefix);
    }
}
