<template>
	<view class="container">
		<!-- 轮播图 -->
		<u-swiper
			:list="swiperList"
			:height="300"
			:indicator="true"
			indicatorMode="line"
			circular
			:autoplay="true"
			:interval="3000"
			:duration="500"
			radius="10"
		></u-swiper>

		<!-- 宫格导航 -->
		<view class="grid-container">
			<u-grid :border="false" col="3">
				<u-grid-item
					v-for="(item, index) in gridList"
					:key="index"
					@click="handleGridClick(item)"
				>
					<view class="grid-item">
						<u-icon :name="item.icon" :size="50" :color="item.color"></u-icon>
						<text class="grid-text">{{ item.name }}</text>
					</view>
				</u-grid-item>
			</u-grid>
		</view>
	</view>
</template>

<script setup>
import { ref } from 'vue'

// 轮播图数据
const swiperList = ref([
	{
		image: 'https://cdn.uviewui.com/uview/swiper/1.jpg',
		title: '宠物救助'
	},
	{
		image: 'https://cdn.uviewui.com/uview/swiper/2.jpg',
		title: '爱心领养'
	},
	{
		image: 'https://cdn.uviewui.com/uview/swiper/3.jpg',
		title: '宠物百科'
	}
])

// 宫格导航数据
const gridList = ref([
	{
		name: '领养',
		icon: 'heart',
		color: '#ff6b6b',
		path: '/pages/adopt/index'
	},
	{
		name: '百科',
		icon: 'book',
		color: '#4ecdc4',
		path: '/pages/wiki/index'
	},
	{
		name: '我的',
		icon: 'account',
		color: '#95e1d3',
		path: '/pages/mine/index'
	}
])

// 宫格点击事件
const handleGridClick = (item) => {
	uni.showToast({
		title: `点击了${item.name}`,
		icon: 'none'
	})
	// 如果需要跳转，可以取消下面的注释
	// if (item.path) {
	// 	uni.navigateTo({
	// 		url: item.path
	// 	})
	// }
}
</script>

<style lang="scss" scoped>
.container {
	padding: 20rpx;
	background-color: #f5f5f5;
	min-height: 100vh;
}

.grid-container {
	margin-top: 30rpx;
	background-color: #fff;
	border-radius: 20rpx;
	padding: 30rpx 0;
}

.grid-item {
	display: flex;
	flex-direction: column;
	align-items: center;
	justify-content: center;
	padding: 30rpx 0;
}

.grid-text {
	margin-top: 20rpx;
	font-size: 28rpx;
	color: #333;
}
</style>