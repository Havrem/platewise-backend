package com.havrem.platewise.mapper;

import com.havrem.platewise.dto.listSection.ListSectionDto;
import com.havrem.platewise.dto.listSection.UpdateListSectionRequest;
import com.havrem.platewise.entity.ListSection;
import org.mapstruct.BeanMapping;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import org.mapstruct.NullValuePropertyMappingStrategy;
import org.mapstruct.ReportingPolicy;

import java.util.List;

@Mapper(componentModel = "spring")
public interface ListSectionMapper {
    @Mapping(source = "itemList.id", target = "itemListId")
    ListSectionDto toDto(ListSection section);
    List<ListSectionDto> toDtos(List<ListSection> sections);

    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE, unmappedTargetPolicy = ReportingPolicy.IGNORE)
    void update(@MappingTarget ListSection target, UpdateListSectionRequest source);
}
