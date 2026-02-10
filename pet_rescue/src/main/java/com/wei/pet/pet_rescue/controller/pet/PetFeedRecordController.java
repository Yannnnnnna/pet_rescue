package com.wei.pet.pet_rescue.controller.pet;


import com.wei.pet.pet_rescue.common.Result;
import com.wei.pet.pet_rescue.entity.dto.pet.PetFeedDTO;
import com.wei.pet.pet_rescue.service.IPetFeedRecordService;
import com.wei.pet.pet_rescue.service.IPetInfoService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;

import org.springframework.web.bind.annotation.RestController;

/**
 * <p>
 * 宠物投喂记录表 前端控制器
 * </p>
 *
 * @author yanna
 * @since 2026-02-09
 */
@RestController
@RequestMapping("/pet-feed-record")
@Tag(name = "宠物投喂记录管理")
@RequiredArgsConstructor
public class PetFeedRecordController {

    private final IPetFeedRecordService petFeedRecordService;
    @Operation(summary = "投喂宠物小鱼干")
    @PostMapping("/feed")
    public Result<Integer> feedPet(@RequestBody PetFeedDTO feedDTO) {
        Integer count = petFeedRecordService.feedPet(feedDTO);
        return count > 0 ? Result.success(count) : Result.error("投喂失败");
    }

}
