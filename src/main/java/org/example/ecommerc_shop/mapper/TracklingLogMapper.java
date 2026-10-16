package org.example.ecommerc_shop.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.example.ecommerc_shop.dto.response.TrackingLogResponse;
import org.example.ecommerc_shop.entity.TrackingLog;

@Mapper(componentModel =  "spring")
public interface TracklingLogMapper {
    @Mapping(target = "id", source = "id")
    TrackingLogResponse toTrackingLogResponse(TrackingLog trackingLog);
}
