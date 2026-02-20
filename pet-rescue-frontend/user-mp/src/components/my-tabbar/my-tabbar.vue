<template>
    <view>
        <u-tabbar 
            :value="current" 
            @change="handleChange" 
            :fixed="true" 
            :placeholder="true" 
            :safeAreaInsetBottom="true" 
            activeColor="#1aad19" 
            inactiveColor="#999999" 
            :zIndex="999"
        >
            <u-tabbar-item text="首页">
                <template #active-icon>
                    <text class="custom-icon custom-icon-index-copy" style="font-size: 44rpx; color: #1aad19;"></text>
                </template>
                <template #inactive-icon>
                    <text class="custom-icon custom-icon-shouye" style="font-size: 44rpx; color: #999999;"></text>
                </template>
            </u-tabbar-item> 
            
            <u-tabbar-item text="消息">
                <template #active-icon>
                    <text class="custom-icon custom-icon-navbar_xiaoxi_xuanzhong" style="font-size: 44rpx; color: #1aad19;"></text>
                </template>
                <template #inactive-icon>
                    <text class="custom-icon custom-icon-xiaoxi" style="font-size: 44rpx; color: #999999;"></text>
                </template>
            </u-tabbar-item> 
            
            <u-tabbar-item text="AI">
                <template #active-icon>
                    <text class="custom-icon custom-icon-jiqiren-xuanzhong" style="font-size: 44rpx; color: #1aad19;"></text>
                </template>
                <template #inactive-icon>
                    <text class="custom-icon custom-icon-jiqiren" style="font-size: 44rpx; color: #999999;"></text>
                </template>
            </u-tabbar-item> 
            
            <u-tabbar-item text="我的">
                <template #active-icon>
                    <text class="custom-icon custom-icon-wode1" style="font-size: 44rpx; color: #1aad19;"></text>
                </template>
                <template #inactive-icon>
                    <text class="custom-icon custom-icon-wode" style="font-size: 44rpx; color: #999999;"></text>
                </template>
            </u-tabbar-item> 
        </u-tabbar> 
    </view> 
</template>

<script>
    export default { 
        name: 'my-tabbar', 
        props: { 
            current: { 
                type: Number, 
                default: 0 
            } 
        }, 
        data() { 
            return { 
                list: [ 
                    '/pages/index/index', 
                    '/pages/message/message', 
                    '/pages/ai/ai', 
                    '/pages/profile/profile' 
                ] 
            } 
        }, 
        methods: { 
            handleChange(index) { 
                // 防止重复点击当前 tab
                if (this.current === index) return;
                
                uni.switchTab({ 
                    url: this.list[index] 
                }) 
            } 
        } 
    } 
</script>