package com.havrem.platewise.mapper;

import com.havrem.platewise.dto.response.ItemListDto;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface ItemListMapper {
    ItemListDto itemListToDto();
}
