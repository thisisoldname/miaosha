package org.example.serializer;
import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.SerializerProvider;
import org.joda.time.DateTime;

import java.io.IOException;

/***************************
 *Author:ct
 *Time:2020/4/19 1:03
 *Dec:Todo
 ****************************/
public class JodaDateTimeSerializer extends JsonSerializer<DateTime> {

    @Override
    public void serialize(DateTime dateTime, JsonGenerator jsonGenerator, SerializerProvider serializerProvider) throws IOException {

        jsonGenerator.writeString(dateTime.toString("yyyy-MM-dd HH:mm:ss"));
    }
}
