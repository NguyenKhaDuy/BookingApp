package com.example.bookingapp.Config;
import org.modelmapper.ModelMapper;
import org.modelmapper.convention.MatchingStrategies;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class ModelMapperConfig {
    @Bean
    public ModelMapper ModalMapperConfig(){
        ModelMapper modelMapper = new ModelMapper();
        modelMapper.getConfiguration()

                // Chỉ ánh xạ khi tên thuộc tính khớp chặt chẽ
                .setMatchingStrategy(MatchingStrategies.STRICT)

                // Không tự ánh xạ vào các thuộc tính lồng nhau
                .setPreferNestedProperties(false)

                // Không bỏ qua lỗi ánh xạ mơ hồ
                .setAmbiguityIgnored(false)

                // Bỏ qua giá trị null từ source
                .setSkipNullEnabled(true)

                // Chỉ sử dụng getter/setter
                .setFieldMatchingEnabled(false);
        return modelMapper;
    }
}
