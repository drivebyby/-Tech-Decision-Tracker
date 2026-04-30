<script setup lang="ts">
import * as echarts from 'echarts'
import { onBeforeUnmount, onMounted, ref } from 'vue'

const chartRef = ref<HTMLDivElement>()
let chart: echarts.ECharts | null = null

function renderChart() {
  if (!chartRef.value) {
    return
  }

  chart = echarts.init(chartRef.value)
  chart.setOption({
    color: ['#0d6c63', '#f08c44'],
    tooltip: {
      trigger: 'axis'
    },
    grid: {
      top: 36,
      left: 28,
      right: 18,
      bottom: 24
    },
    legend: {
      top: 0
    },
    xAxis: {
      type: 'category',
      boundaryGap: false,
      data: ['周一', '周二', '周三', '周四', '周五', '周六', '周日']
    },
    yAxis: {
      type: 'value'
    },
    series: [
      {
        name: '访问量',
        type: 'line',
        smooth: true,
        areaStyle: {
          opacity: 0.18
        },
        data: [120, 180, 160, 240, 320, 280, 360]
      },
      {
        name: '新增用户',
        type: 'bar',
        data: [12, 18, 14, 22, 31, 24, 36]
      }
    ]
  })
}

function resizeChart() {
  chart?.resize()
}

onMounted(() => {
  renderChart()
  window.addEventListener('resize', resizeChart)
})

onBeforeUnmount(() => {
  window.removeEventListener('resize', resizeChart)
  chart?.dispose()
})
</script>

<template>
  <div ref="chartRef" class="chart"></div>
</template>

<style scoped>
.chart {
  min-height: 320px;
}
</style>
