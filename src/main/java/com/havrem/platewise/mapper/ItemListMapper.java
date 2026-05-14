package com.havrem.platewise.mapper;

import com.havrem.platewise.dto.itemList.ItemListDto;
import com.havrem.platewise.entity.ItemList;
import org.mapstruct.Mapper;

import java.util.List;

@Mapper(componentModel = "spring", uses = { CategoryMapper.class, ItemMapper.class})
public interface ItemListMapper {
    ItemListDto toDto(ItemList itemList);
    List<ItemListDto> toDtos(List<ItemList> itemLists);
}
