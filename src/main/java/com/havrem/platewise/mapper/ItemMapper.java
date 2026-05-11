package com.havrem.platewise.mapper;

import com.havrem.platewise.dto.response.ItemDto;
import com.havrem.platewise.entity.Item;
import org.mapstruct.Mapper;

import java.util.List;

@Mapper(componentModel = "spring")
public interface ItemMapper {
    ItemDto toDto(Item item);
    List<ItemDto> toDtos(List<Item> items);
}
