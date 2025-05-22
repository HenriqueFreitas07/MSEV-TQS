package tqs.msev.backend.service;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestTemplate;
import tqs.msev.backend.dto.Coordinates;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class GeocodingServiceTest {
    @Mock
    private RestTemplate restTemplate;

    @InjectMocks
    private GeocodingService service;

    private static final String MOCK_RESPONSE = "{\"results\":[{\"datasource\":{\"sourcename\":\"openstreetmap\",\"attribution\":\"© OpenStreetMap contributors\",\"license\":\"Open Database License\",\"url\":\"https://www.openstreetmap.org/copyright\"},\"name\":\"Avenida da Universidade\",\"ref\":\"EN 235\",\"country\":\"Portugal\",\"country_code\":\"pt\",\"county\":\"Aveiro\",\"city\":\"Aveiro\",\"village\":\"Aradas\",\"hamlet\":\"Quinta do Casal\",\"postcode\":\"3810-489\",\"street\":\"Avenida da Universidade\",\"iso3166_2\":\"PT-01\",\"lon\":-8.6514061,\"lat\":40.6254255,\"result_type\":\"street\",\"formatted\":\"Avenida da Universidade, 3810-489 Aveiro, Portugal\",\"address_line1\":\"Avenida da Universidade\",\"address_line2\":\"3810-489 Aveiro, Portugal\",\"timezone\":{\"name\":\"Europe/Lisbon\",\"offset_STD\":\"+00:00\",\"offset_STD_seconds\":0,\"offset_DST\":\"+01:00\",\"offset_DST_seconds\":3600,\"abbreviation_STD\":\"WET\",\"abbreviation_DST\":\"WEST\"},\"plus_code\":\"8CGHJ8GX+5C\",\"plus_code_short\":\"J8GX+5C Aveiro, Portugal\",\"rank\":{\"importance\":0.4633433333333333,\"popularity\":5.547977036397171,\"confidence\":1,\"confidence_city_level\":1,\"confidence_street_level\":1,\"match_type\":\"full_match\"},\"place_id\":\"516fd4af19854d21c059cf4a5af10d504440f00102f9011d3fb70800000000c002049203174176656e69646120646120556e69766572736964616465\",\"bbox\":{\"lon1\":-8.6518619,\"lat1\":40.6249893,\"lon2\":-8.650676,\"lat2\":40.6260056}},{\"datasource\":{\"sourcename\":\"openstreetmap\",\"attribution\":\"© OpenStreetMap contributors\",\"license\":\"Open Database License\",\"url\":\"https://www.openstreetmap.org/copyright\"},\"name\":\"Avenida da Universidade\",\"ref\":\"EN 235\",\"country\":\"Portugal\",\"country_code\":\"pt\",\"county\":\"Aveiro\",\"city\":\"Aveiro\",\"postcode\":\"3810-193\",\"district\":\"Glória e Vera Cruz\",\"suburb\":\"Glória\",\"street\":\"Avenida da Universidade\",\"iso3166_2\":\"PT-01\",\"lon\":-8.6548105,\"lat\":40.6315476,\"result_type\":\"street\",\"formatted\":\"Avenida da Universidade, 3810-193 Aveiro, Portugal\",\"address_line1\":\"Avenida da Universidade\",\"address_line2\":\"3810-193 Aveiro, Portugal\",\"timezone\":{\"name\":\"Europe/Lisbon\",\"offset_STD\":\"+00:00\",\"offset_STD_seconds\":0,\"offset_DST\":\"+01:00\",\"offset_DST_seconds\":3600,\"abbreviation_STD\":\"WET\",\"abbreviation_DST\":\"WEST\"},\"plus_code\":\"8CGHJ8JW+J3\",\"plus_code_short\":\"JW+J3 Aveiro, Portugal\",\"rank\":{\"importance\":0.4633433333333333,\"popularity\":6.246459893126748,\"confidence\":1,\"confidence_city_level\":1,\"confidence_street_level\":1,\"match_type\":\"full_match\"},\"place_id\":\"51a2276552434f21c05903ef3f8dd6504440f00102f9017cf4661d00000000c002049203174176656e69646120646120556e69766572736964616465\",\"bbox\":{\"lon1\":-8.6557701,\"lat1\":40.6308209,\"lon2\":-8.6543825,\"lat2\":40.6331453}},{\"datasource\":{\"sourcename\":\"openstreetmap\",\"attribution\":\"© OpenStreetMap contributors\",\"license\":\"Open Database License\",\"url\":\"https://www.openstreetmap.org/copyright\"},\"name\":\"Avenida da Universidade\",\"ref\":\"EN 235\",\"country\":\"Portugal\",\"country_code\":\"pt\",\"county\":\"Aveiro\",\"city\":\"Aveiro\",\"hamlet\":\"Pêga\",\"postcode\":\"3810-074\",\"district\":\"Glória e Vera Cruz\",\"street\":\"Avenida da Universidade\",\"iso3166_2\":\"PT-01\",\"lon\":-8.6559809,\"lat\":40.6348907,\"result_type\":\"street\",\"formatted\":\"Avenida da Universidade, 3810-074 Aveiro, Portugal\",\"address_line1\":\"Avenida da Universidade\",\"address_line2\":\"3810-074 Aveiro, Portugal\",\"timezone\":{\"name\":\"Europe/Lisbon\",\"offset_STD\":\"+00:00\",\"offset_STD_seconds\":0,\"offset_DST\":\"+01:00\",\"offset_DST_seconds\":3600,\"abbreviation_STD\":\"WET\",\"abbreviation_DST\":\"WEST\"},\"plus_code\":\"8CGHJ8MV+XJ\",\"plus_code_short\":\"MV+XJ Aveiro, Portugal\",\"rank\":{\"importance\":0.4633433333333333,\"popularity\":6.170725657914504,\"confidence\":1,\"confidence_city_level\":1,\"confidence_street_level\":1,\"match_type\":\"full_match\"},\"place_id\":\"51ee9980badc4f21c0596c84341944514440f00102f90101b32c5000000000c002049203174176656e69646120646120556e69766572736964616465\",\"bbox\":{\"lon1\":-8.6561655,\"lat1\":40.6347212,\"lon2\":-8.6558369,\"lat2\":40.6351758}},{\"datasource\":{\"sourcename\":\"openstreetmap\",\"attribution\":\"© OpenStreetMap contributors\",\"license\":\"Open Database License\",\"url\":\"https://www.openstreetmap.org/copyright\"},\"name\":\"Avenida da Universidade\",\"ref\":\"EN 235\",\"country\":\"Portugal\",\"country_code\":\"pt\",\"county\":\"Aveiro\",\"city\":\"Aveiro\",\"hamlet\":\"Pêga\",\"postcode\":\"3814-506\",\"district\":\"Glória e Vera Cruz\",\"street\":\"Avenida da Universidade\",\"iso3166_2\":\"PT-01\",\"lon\":-8.6563829,\"lat\":40.6341511,\"result_type\":\"street\",\"formatted\":\"Avenida da Universidade, 3814-506 Aveiro, Portugal\",\"address_line1\":\"Avenida da Universidade\",\"address_line2\":\"3814-506 Aveiro, Portugal\",\"timezone\":{\"name\":\"Europe/Lisbon\",\"offset_STD\":\"+00:00\",\"offset_STD_seconds\":0,\"offset_DST\":\"+01:00\",\"offset_DST_seconds\":3600,\"abbreviation_STD\":\"WET\",\"abbreviation_DST\":\"WEST\"},\"plus_code\":\"8CGHJ8MV+MC\",\"plus_code_short\":\"MV+MC Aveiro, Portugal\",\"rank\":{\"importance\":0.4633433333333333,\"popularity\":6.117852992400655,\"confidence\":1,\"confidence_city_level\":1,\"confidence_street_level\":1,\"match_type\":\"full_match\"},\"place_id\":\"51aa4e626b115021c059789cfddc2b514440f00102f901676e4d0200000000c002049203174176656e69646120646120556e69766572736964616465\",\"bbox\":{\"lon1\":-8.6564109,\"lat1\":40.6336053,\"lon2\":-8.6560551,\"lat2\":40.6347212}}],\"query\":{\"text\":\"Avenida da Universidade Aveiro\",\"parsed\":{\"street\":\"avenida da universidade\",\"city\":\"aveiro\",\"expected_type\":\"street\"}}}";

    @Test
    void whenGetWeather_thenReturnWeatherData() {
        when(restTemplate.getForEntity(Mockito.anyString(), Mockito.eq(String.class))).thenReturn(new ResponseEntity<>(MOCK_RESPONSE, HttpStatus.OK));

        Coordinates coordinates = service.getCoordinatesForAddress("Avenida da Universidade Aveiro");

        assertThat(coordinates.getLon()).isEqualTo(-8.6514061);
        assertThat(coordinates.getLat()).isEqualTo(40.6254255);

        verify(restTemplate, times(1)).getForEntity(Mockito.anyString(), Mockito.eq(String.class));
    }
}
