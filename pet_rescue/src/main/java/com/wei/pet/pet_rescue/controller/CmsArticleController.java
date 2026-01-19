package com.wei.pet.pet_rescue.controller;


import com.baomidou.mybatisplus.core.metadata.IPage;
import com.wei.pet.pet_rescue.common.Result;
import com.wei.pet.pet_rescue.entity.CmsArticle;
import com.wei.pet.pet_rescue.entity.dto.ArticleFormDTO;
import com.wei.pet.pet_rescue.entity.dto.ArticleQueryDTO;
import com.wei.pet.pet_rescue.service.ICmsArticleService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.Resource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

/**
 * <p>
 * 文章/百科表 前端控制器
 * </p>
 *
 * @author yanna
 * @since 2026-01-10
 */
@Tag(name = "内容生态-百科与壁纸")
@RestController
@RequestMapping("/cms-article")
public class CmsArticleController {
    @Resource
    private ICmsArticleService cmsArticleService;

    @Operation(summary = "发布/修改文章", description = "支持百科(type=0)、公告(type=1)、壁纸(type=3)")
    @PostMapping("/save")
    public Result<Boolean> save(@RequestBody @Validated ArticleFormDTO dto) {
        boolean success = cmsArticleService.saveArticle(dto);
        return success ? Result.success(true) : Result.error("操作失败");
    }

    @Operation(summary = "删除文章")
    @DeleteMapping("/delete/{id}")
    public Result<Boolean> delete(@PathVariable Long id) {
        return cmsArticleService.removeById(id) ? Result.success(true) : Result.error("删除失败");
    }

    @Operation(summary = "文章/壁纸列表查询", description = "前端想看壁纸就传type=3，想看百科就传type=0")
    @PostMapping("/list")
    public Result<IPage<CmsArticle>> list(@RequestBody ArticleQueryDTO query) {
        IPage<CmsArticle> page = cmsArticleService.getArticlePage(query);
        return Result.success(page);
    }

    @Operation(summary = "获取详情", description = "阅读文章或查看大图")
    @GetMapping("/{id}")
    public Result<CmsArticle> getDetail(@PathVariable Long id) {
        CmsArticle article = cmsArticleService.getById(id);

        // 增加阅读量 (简单的 +1 操作，高并发下需优化，毕设这样写没问题)
        if (article != null) {
            article.setViewCount(article.getViewCount() + 1);
            cmsArticleService.updateById(article);
        }
        return Result.success(article);
    }
}
