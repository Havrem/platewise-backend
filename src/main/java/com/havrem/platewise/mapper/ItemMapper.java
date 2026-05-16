package com.havrem.platewise.mapper;

import com.havrem.platewise.dto.item.ItemDto;
import com.havrem.platewise.dto.item.UpdateItemRequest;
import com.havrem.platewise.entity.Item;
import org.mapstruct.BeanMapping;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import org.mapstruct.NullValuePropertyMappingStrategy;
import org.mapstruct.ReportingPolicy;

import java.util.List;

@Mapper(componentModel = "spring")
public interface ItemMapper {
    @Mapping(source = "itemList.id", target = "itemListId")
    ItemDto toDto(Item item);
    List<ItemDto> toDtos(List<Item> items);

    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE, unmappedTargetPolicy = ReportingPolicy.IGNORE)
    void update(@MappingTarget Item target, UpdateItemRequest source);
}
