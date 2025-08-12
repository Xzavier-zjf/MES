/**
 * 任务分配建议工具
 * 基于工序类型和进度提供智能分配建议
 */

import { PROCESS_FLOW, calculateDetailedAllocation, calculateAvailableQuantity } from './productionFlowCalculator'

/**
 * 生成任务分配建议
 * @param {Object} plan 生产计划
 * @param {Array} tasks 现有任务列表
 * @param {Array} availableDevices 可用设备列表
 * @returns {Array} 分配建议列表
 */
export function generateAllocationSuggestions(plan, tasks, availableDevices = []) {
    const allocation = calculateDetailedAllocation(plan, tasks)
    const suggestions = []

    // 为每个工序生成建议
    PROCESS_FLOW.forEach(processType => {
        const availableInfo = calculateAvailableQuantity(processType, plan, tasks)
        const processSummary = allocation.processSummary[processType]

        if (availableInfo.available > 0) {
            // 找到适合的设备
            const suitableDevices = findSuitableDevices(processType, availableDevices)

            suggestions.push({
                processType,
                availableQuantity: availableInfo.available,
                maxAvailable: availableInfo.maxAvailable,
                currentAllocated: availableInfo.currentAllocated,
                suitableDevices: suitableDevices.slice(0, 3), // 最多推荐3个设备
                priority: calculatePriority(processType, allocation, availableInfo),
                reason: generateReason(processType, allocation, availableInfo),
                suggestedQuantity: calculateSuggestedQuantity(availableInfo.available, suitableDevices.length),
                dependsOn: availableInfo.dependsOn,
                canAllocateNow: availableInfo.canAllocate
            })
        }
    })

    // 按优先级排序
    return suggestions.sort((a, b) => b.priority - a.priority)
}

/**
 * 找到适合指定工序的设备
 * @param {string} processType 工序类型
 * @param {Array} devices 设备列表
 * @returns {Array} 适合的设备列表
 */
function findSuitableDevices(processType, devices) {
    const devicePrefixes = {
        '注塑': ['INJ'],
        '印刷': ['PRT'],
        '组装': ['ASM'],
        '质检': ['QC'],
        '包装': ['PKG']
    }

    const prefixes = devicePrefixes[processType] || []

    return devices.filter(device => {
        // 检查设备编号前缀
        const matchesPrefix = prefixes.length === 0 || prefixes.some(prefix =>
            device.deviceCode && device.deviceCode.startsWith(prefix)
        )

        // 检查设备状态（空闲设备优先）
        const isAvailable = device.status === '空闲'

        return matchesPrefix && isAvailable
    }).sort((a, b) => {
        // 空闲设备优先，然后按设备编号排序
        if (a.status !== b.status) {
            return a.status === '空闲' ? -1 : 1
        }
        return (a.deviceCode || '').localeCompare(b.deviceCode || '')
    })
}

/**
 * 计算分配优先级
 * @param {string} processType 工序类型
 * @param {Object} allocation 分配情况
 * @param {Object} availableInfo 可用数量信息
 * @returns {number} 优先级分数 (0-100)
 */
function calculatePriority(processType, allocation, availableInfo) {
    let priority = 50 // 基础优先级

    // 工序顺序影响优先级（越早的工序优先级越高）
    const processIndex = PROCESS_FLOW.indexOf(processType)
    priority += (PROCESS_FLOW.length - processIndex) * 10

    // 可分配数量影响优先级
    const quantityRatio = availableInfo.available / allocation.totalQuantity
    priority += quantityRatio * 20

    // 如果是瓶颈工序，提高优先级
    if (allocation.flowProgress[processType]?.bottleneck) {
        priority += 15
    }

    // 如果前序工序已完成较多，提高优先级
    if (processIndex > 0) {
        const prevProcess = PROCESS_FLOW[processIndex - 1]
        const prevCompletionRate = allocation.flowProgress[prevProcess]?.completionRate || 0
        if (prevCompletionRate > 50) {
            priority += 10
        }
    }

    return Math.min(100, Math.max(0, priority))
}

/**
 * 生成分配原因说明
 * @param {string} processType 工序类型
 * @param {Object} allocation 分配情况
 * @param {Object} availableInfo 可用数量信息
 * @returns {string} 原因说明
 */
function generateReason(processType, allocation, availableInfo) {
    const reasons = []

    if (availableInfo.dependsOn) {
        const prevProcess = availableInfo.dependsOn
        const prevCompleted = allocation.processSummary[prevProcess].completedQuantity
        reasons.push(`${prevProcess}工序已完成${prevCompleted}个`)
    }

    if (allocation.flowProgress[processType]?.bottleneck) {
        reasons.push('识别为瓶颈工序，需要优先处理')
    }

    const processIndex = PROCESS_FLOW.indexOf(processType)
    if (processIndex === 0) {
        reasons.push('首工序，可以立即开始')
    }

    if (availableInfo.available === availableInfo.maxAvailable) {
        reasons.push('可以分配全部剩余数量')
    }

    return reasons.join('；') || '建议分配任务以推进生产进度'
}

/**
 * 计算建议分配数量
 * @param {number} available 可用数量
 * @param {number} deviceCount 可用设备数量
 * @returns {number} 建议数量
 */
function calculateSuggestedQuantity(available, deviceCount) {
    if (deviceCount === 0) return 0

    // 根据设备数量平均分配，但不超过可用数量
    const avgPerDevice = Math.ceil(available / deviceCount)

    // 单个任务建议不超过总量的50%，最少10个
    const maxPerTask = Math.max(10, Math.floor(available * 0.5))
    const minPerTask = Math.min(10, available)

    return Math.max(minPerTask, Math.min(maxPerTask, avgPerDevice))
}

/**
 * 生成任务创建模板
 * @param {Object} suggestion 分配建议
 * @param {Object} plan 生产计划
 * @returns {Array} 任务模板列表
 */
export function generateTaskTemplates(suggestion, plan) {
    const templates = []

    suggestion.suitableDevices.forEach((device, index) => {
        const quantity = index === 0 ? suggestion.suggestedQuantity :
            Math.min(suggestion.suggestedQuantity, suggestion.availableQuantity - suggestion.suggestedQuantity)

        if (quantity > 0) {
            templates.push({
                taskCode: `${plan.planCode}-${suggestion.processType}-${String(index + 1).padStart(2, '0')}`,
                planId: plan.id,
                planCode: plan.planCode,
                processType: suggestion.processType,
                deviceId: device.id,
                deviceCode: device.deviceCode,
                quantity: quantity,
                status: '待下发',
                priority: suggestion.priority,
                estimatedDuration: estimateTaskDuration(suggestion.processType, quantity),
                dependencies: suggestion.dependsOn ? [`${plan.planCode}-${suggestion.dependsOn}`] : []
            })
        }
    })

    return templates
}

/**
 * 估算任务持续时间
 * @param {string} processType 工序类型
 * @param {number} quantity 数量
 * @returns {number} 估算小时数
 */
function estimateTaskDuration(processType, quantity) {
    // 每个工序的单位时间（分钟/件）
    const timePerUnit = {
        '注塑': 2,
        '印刷': 1.5,
        '组装': 3,
        '质检': 1,
        '包装': 0.5
    }

    const unitTime = timePerUnit[processType] || 2
    const totalMinutes = quantity * unitTime

    // 转换为小时，向上取整
    return Math.ceil(totalMinutes / 60)
}

/**
 * 验证分配建议的可行性
 * @param {Array} suggestions 分配建议列表
 * @param {Object} plan 生产计划
 * @param {Array} tasks 现有任务
 * @returns {Object} 验证结果
 */
export function validateAllocationSuggestions(suggestions, plan, tasks) {
    const issues = []
    const warnings = []

    // 检查总分配数量是否超过计划
    const totalSuggested = suggestions.reduce((sum, s) => sum + s.suggestedQuantity, 0)
    const currentAllocated = tasks.filter(t => t.planId === plan.id)
        .reduce((sum, t) => sum + (t.quantity || 0), 0)

    if (totalSuggested + currentAllocated > plan.totalQuantity) {
        issues.push({
            type: 'quantity_exceeded',
            message: `建议分配总量(${totalSuggested})加上已分配(${currentAllocated})超过计划总量(${plan.totalQuantity})`
        })
    }

    // 检查设备冲突
    const deviceUsage = new Map()
    suggestions.forEach(suggestion => {
        suggestion.suitableDevices.forEach(device => {
            if (deviceUsage.has(device.id)) {
                warnings.push({
                    type: 'device_conflict',
                    message: `设备${device.deviceCode}被多个工序建议使用`
                })
            } else {
                deviceUsage.set(device.id, suggestion.processType)
            }
        })
    })

    // 检查工序依赖
    suggestions.forEach(suggestion => {
        if (suggestion.dependsOn && !suggestion.canAllocateNow) {
            warnings.push({
                type: 'dependency_not_ready',
                message: `${suggestion.processType}工序依赖${suggestion.dependsOn}工序，但前序工序尚未完成足够数量`
            })
        }
    })

    return {
        isValid: issues.length === 0,
        issues,
        warnings,
        totalSuggested,
        feasibleSuggestions: suggestions.filter(s => s.canAllocateNow)
    }
}