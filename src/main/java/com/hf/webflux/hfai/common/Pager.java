package com.hf.webflux.hfai.common;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;
import lombok.experimental.SuperBuilder;
import java.util.List;

@EqualsAndHashCode(callSuper = false)
@AllArgsConstructor
@NoArgsConstructor
@SuperBuilder
@Data
@Accessors(chain = true)
public class Pager<T> {
    /**
     * 当前页数
     */
    @NotBlank
    private Long page;

    /**
     * 每页显示的行数
     */
    @NotBlank
    private Long size;

    /**
     * 是否到最后一页
     */
    private boolean isLast;

    public void setLast(boolean last) {
    }

    public boolean isLast() {
        return records == null || records.size() < size;
    }

    /**
     * 数据
     */
    private List<T> records;


    public Pager(PageRequest<T> pageRequest) {
        this.page = pageRequest.getPage();
        this.size = pageRequest.getSize();
    }

    public Pager(Long page, Long size) {
        this.page = page;
        this.size = size;
    }


}
