package com.havrem.platewise.mapper;

import com.havrem.platewise.dto.itemList.ItemListDto;
import com.havrem.platewise.dto.itemList.UpdateItemListRequest;
import com.havrem.platewise.entity.ItemList;
import org.mapstruct.BeanMapping;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import org.mapstruct.NullValuePropertyMappingStrategy;
import org.mapstruct.ReportingPolicy;

import java.util.List;

@Mapper(componentModel = "spring", uses = { CategoryMapper.class, ItemMapper.class})
public interface ItemListMapper {
    ItemListDto toDto(ItemList itemList);
    List<ItemListDto> toDtos(List<ItemList> itemLists);

    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE, unmappedTargetPolicy = ReportingPolicy.IGNORE)
    @Mapping(target = "category", ignore = true)
    void update(@MappingTarget ItemList target, UpdateItemListRequest source);
}
