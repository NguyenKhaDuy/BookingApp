package com.example.bookingapp.Config;
import org.modelmapper.ModelMapper;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class ModelMapperConfig {
    @Bean
    public ModelMapper ModalMapperConfig(){
        ModelMapper modelMapper = new ModelMapper();
        // Bỏ qua field evaluate, tự set sau
//        modelMapper.typeMap(ProductEntity.class, ProductDTO.class).addMappings(mapper -> {
//            mapper.skip(ProductDTO::setEvaluate);
//        });
        return modelMapper;
    }
}
