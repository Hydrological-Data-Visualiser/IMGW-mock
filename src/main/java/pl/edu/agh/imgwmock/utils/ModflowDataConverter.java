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
import java.util.stream.Collectors;

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

    private int LAYER = 3;

    public List<String> possibleDates = List.of(
            "2017-04-17T00:00:00.00Z",
            "2017-07-17T00:00:00.00Z",
            "2017-10-17T00:00:00.00Z",
            "2018-01-17T00:00:00.00Z",
            "2018-04-17T00:00:00.00Z",
            "2018-07-17T00:00:00.00Z",
            "2018-10-17T00:00:00.00Z",
            "2019-01-17T00:00:00.00Z",
            "2019-04-17T00:00:00.00Z",
            "2019-07-17T00:00:00.00Z",
            "2019-10-17T00:00:00.00Z",
            "2020-01-17T00:00:00.00Z",
            "2020-04-17T00:00:00.00Z"
    );

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
        List result = new ArrayList();
        for(int stressPeriod = 0; stressPeriod < 13; stressPeriod++) {
            result.addAll(convertDataToPolygons(stressPeriod, LAYER));
        }
        return result;
    }

    public String getTimePointAfter(String instantFrom, int step) {
        int index = 0;
        for (int i = 0; i < possibleDates.size(); i++) {
            if (compareStringInstant(instantFrom, i)) {
                index = i;
                break;
            }
        }
        return possibleDates.get(index + step < possibleDates.size() ? index + step : 12);
    }

    public Double getMinValueOnInterval(String instantFrom, int interval) {
        int index = 0;
        for (int i = 0; i < possibleDates.size(); i++) {
            if (compareStringInstant(instantFrom, i)) {
                index = i;
                break;
            }
        }
        Double min = 999.0;
        for (int i = index; i < index + interval && i < possibleDates.size(); i++) {
            Double tmpMinValue = getMinValue(i, LAYER);
            min = min > tmpMinValue ? tmpMinValue : min;
        }
        return min;
    }

    public Double getMaxValueOnInterval(String instantFrom, int interval) {
        int index = 0;
        for (int i = 0; i < possibleDates.size(); i++) {
            if (compareStringInstant(instantFrom, i)) {
                index = i;
                break;
            }
        }
        Double max = -999.0;
        for (int i = index; i < index + interval && i < possibleDates.size(); i++) {
            Double tmpMaxValue = getMaxValue(i, LAYER);
            max = max < tmpMaxValue ? tmpMaxValue : max;
        }
        return max;
    }

    private boolean compareStringInstant(String instantFrom, int index) {
        return possibleDates.get(index).substring(0, 10).equals(instantFrom.substring(0, 10));
    }

    private Double getMinValue(int stressPeriod, int layerNumber) {
        var layer = data.get(stressPeriod).get(layerNumber);
        Double min = 999.0;
        for (var row : layer) {
            for (var val : row) {
                if (min > val && Double.compare(val, -999.0) != 0) min = val;
            }
        }
        return min;
    }

    private Double getMaxValue(int stressPeriod, int layerNumber) {
        var layer = data.get(stressPeriod).get(layerNumber);
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
        return possibleDates.stream()
                .map(d -> LocalDate.parse(d.substring(0, 10)))
                .collect(Collectors.toList());
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

    private List<PolygonDataNew> convertDataToPolygons(int stressPeriod, int layerNumber) {
        var layer = data.get(stressPeriod).get(layerNumber);
        Long id = 0L;
        List<PolygonDataNew> result = new ArrayList<>();
        for (int i = 0; i < layer.size(); i++) {
            for (int j = 0; j < layer.get(i).size(); j++) {
                Double value = layer.get(i).get(j);
                result.add(new PolygonDataNew(
                        id,
                        id,
                        Double.compare(value, -999.0) == 0 ? null : value,
                        Instant.parse(getDate(stressPeriod))));
                id++;
            }
        }
        return result;
    }

    private String getDate(int stressPeriod) {
        return possibleDates.get(stressPeriod);
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
