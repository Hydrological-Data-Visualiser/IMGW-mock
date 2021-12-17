package pl.edu.agh.imgwmock.utils;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.Setter;
import lombok.val;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import pl.edu.agh.imgwmock.model.*;

import java.io.File;
import java.io.IOException;
import java.time.Instant;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Component
@Setter
public class ModflowDataConverter {

    private static final Logger logger = LoggerFactory.getLogger(ModflowDataConverter.class);

    private static final Double METER_TO_DEGREE = 0.000009;

    private final ObjectMapper objectMapper;

    private String pathToDataFile = "src/main/resources/modflow/result.json";

    private String pathToInfoFile = "src/main/resources/modflow/Cekcyn17.json";

    private List<List<List<List<Double>>>> data;

    private ModflowModelInfo info;

    public ModflowDataConverter(String pathToDataFile, String pathToInfoFile) {
        this.pathToDataFile = pathToDataFile;
        this.pathToInfoFile = pathToInfoFile;
        this.objectMapper = new ObjectMapper();
        readData();
    }

    public ModflowDataConverter() {
        this.objectMapper = new ObjectMapper();
        readData();
    }

    public Info getInfo() {
        return new Info(info.getName(), info.getName(), "Modflow model: " + info.getModflow_model(),
                DataType.POLYGON, "[metric]", "#FFF000", "#000FFF", getAvailableDates());
    }

    public List<PolygonDataNew> getData() {
        return convertDataToPolygons(Double.parseDouble(info.getLat()), Double.parseDouble(info.getLongitude()));
    }

    public Double getMinValue() {
        return -999.0;
    }

    public Double getMaxValue() {
        var layer = data.get(0).get(0);
        Double max = -999.0;
        for (var row : layer) {
            for (var val : row) {
                if (max < val) max = val;
            }
        }
        return max;
    }

    private void readData() {
        readDataFromJsonFile();
        readInfoFromJsonFile();
    }

    private List<LocalDate> getAvailableDates() {
        return List.of(LocalDate.parse(info.getStart_date()));
    }

    // xd
    private List<List<List<List<Double>>>> readDataFromJsonFile() {
        data = readFromJsonFile(new TypeReference<List<List<List<List<Double>>>>>() {
        }, pathToDataFile);
        return data;
    }

    private ModflowModelInfo readInfoFromJsonFile() {
        info = readFromJsonFile(new TypeReference<ModflowModelInfo>() {
        }, pathToInfoFile);
        return info;
    }

    private <T> T readFromJsonFile(TypeReference<T> typeReference, String path) {
        try {
            return objectMapper.readValue(new File(path), typeReference);
        } catch (IOException e) {
            logger.error("An error occurred when reading from json file {}", path);
            return null;
        }
    }

    private Double convertMetersToDegree(Double begin, Integer index) {
        return begin + index * METER_TO_DEGREE;
    }

    private List<PolygonDataNew> convertDataToPolygons(Double latitude, Double longitude) {
        var layer = data.get(0).get(0);
        Long id = 0L;
        List<PolygonDataNew> result = new ArrayList<>();
        for (int i = 0; i < layer.size(); i++) {
            for (int j = 0; j < layer.get(i).size(); j++) {
//        for (int i = 0; i < 35; i++) {
//            for (int j = 0; j < 35; j++) {
                result.add(new PolygonDataNew(id, id, layer.get(i).get(j), Instant.parse(info.getStart_date() + "T00:00:00.00Z")));
                id++;
            }
        }
        return result;
    }


    public List<Station> getStations() {
        val latitude = Double.parseDouble(info.getLat());
        val longitude = Double.parseDouble(info.getLongitude());
        var layer = data.get(0).get(0);
        Long id = 0L;
        List<Station> result = new ArrayList<>();
        for (int i = 0; i < layer.size(); i++) {
            for (int j = 0; j < layer.get(i).size(); j++) {
                val list = List.of(
                        new Double[]{convertMetersToDegree(latitude, (-1) * j), convertMetersToDegree(longitude, i)},
                        new Double[]{convertMetersToDegree(latitude, (-1) * j), convertMetersToDegree(longitude, i + 1)},
                        new Double[]{convertMetersToDegree(latitude, (-1) * (j + 1)), convertMetersToDegree(longitude, i + 1)},
                        new Double[]{convertMetersToDegree(latitude, (-1) * (j + 1)), convertMetersToDegree(longitude, i)}
                );
                result.add(new Station(id, "", list));
                id++;
            }
        }
        return result;
    }
}
