package com.havrem.platewise.mapper;

import com.havrem.platewise.dto.item.ItemDto;
import com.havrem.platewise.entity.Item;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import java.util.List;

@Mapper(componentModel = "spring")
public interface ItemMapper {
    @Mapping(source = "itemList.id", target = "itemListId")
    ItemDto toDto(Item item);
    List<ItemDto> toDtos(List<Item> items);
}
