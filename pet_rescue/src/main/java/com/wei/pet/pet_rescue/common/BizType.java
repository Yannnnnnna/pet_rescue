package com.wei.pet.pet_rescue.common;

import lombok.Data;

/**
 * 业务类型枚举类，用于redis
 *
 * @author wyr on 2026/2/19
 */
@Data
public class BizType {
    //宠物
    public static final String PET = "pet";
    public static final String ARTICLE = "article";
  public   static final String DIARY = "diary";
}
