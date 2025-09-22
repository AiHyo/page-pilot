package com.aih.pagepilot.core;

import com.aih.pagepilot.ai.model.HtmlCodeResult;
import com.aih.pagepilot.ai.model.MultiFileCodeResult;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertNotNull;

class CodeParserTest {

    @Test
    void parseHtmlCode() {
        String codeContent = """
                随便写一段描述：
                ```html
                <!DOCTYPE html>
                <html>
                <head>
                    <title>测试页面</title>
                </head>
                <body>
                    <h1>Hello World!</h1>
                </body>
                </html>
                ```
                随便写一段描述
                """;
        HtmlCodeResult result = CodeParser.parseHtmlCode(codeContent);
        assertNotNull(result);
        assertNotNull(result.getHtmlCode());
    }

    @Test
    void parseMultiFileCode() {
        String codeContent = """
                # Todo任务清单应用
                
                我将创建一个简洁但功能完整的Todo任务清单应用，包含添加任务、标记完成、删除任务和过滤功能。
                
                ## 设计思路
                - 极简设计，专注于功能
                - 响应式布局，适配各种设备
                - 使用localStorage保存数据
                - 清晰的视觉反馈
                
                下面是完整的实现代码：
                
                ```html
                <!DOCTYPE html>
                <html lang="zh-CN">
                <head>
                    <meta charset="UTF-8">
                    <meta name="viewport" content="width=device-width, initial-scale=1.0">
                    <title>Todo任务清单</title>
                    <link rel="stylesheet" href="style.css">
                </head>
                <body>
                    <div class="container">
                        <header>
                            <h1>任务清单</h1>
                        </header>
                       \s
                        <div class="input-section">
                            <input type="text" id="taskInput" placeholder="添加新任务...">
                            <button id="addBtn">添加</button>
                        </div>
                       \s
                        <div class="filters">
                            <button class="filter-btn active" data-filter="all">全部</button>
                            <button class="filter-btn" data-filter="active">待完成</button>
                            <button class="filter-btn" data-filter="completed">已完成</button>
                        </div>
                       \s
                        <ul id="taskList"></ul>
                       \s
                        <div class="stats">
                            <span id="itemsLeft">0个项目待完成</span>
                            <button id="clearCompleted">清除已完成</button>
                        </div>
                    </div>
                   \s
                    <script src="script.js"></script>
                </body>
                </html>
                ```
                
                ```css
                * {
                    margin: 0;
                    padding: 0;
                    box-sizing: border-box;
                }
                
                body {
                    font-family: -apple-system, BlinkMacSystemFont, 'Segoe UI', Roboto, Oxygen, sans-serif;
                    background-color: #f5f5f5;
                    color: #333;
                    line-height: 1.6;
                    padding: 20px;
                }
                
                .container {
                    max-width: 600px;
                    margin: 0 auto;
                    background: white;
                    border-radius: 8px;
                    padding: 20px;
                    box-shadow: 0 2px 10px rgba(0, 0, 0, 0.1);
                }
                
                header {
                    text-align: center;
                    margin-bottom: 20px;
                }
                
                h1 {
                    color: #2c3e50;
                    font-weight: 400;
                }
                
                .input-section {
                    display: flex;
                    margin-bottom: 20px;
                }
                
                #taskInput {
                    flex: 1;
                    padding: 10px 15px;
                    border: 1px solid #ddd;
                    border-radius: 4px 0 0 4px;
                    font-size: 16px;
                }
                
                #addBtn {
                    padding: 10px 20px;
                    background: #3498db;
                    color: white;
                    border: none;
                    border-radius: 0 4px 4px 0;
                    cursor: pointer;
                    font-size: 16px;
                }
                
                #addBtn:hover {
                    background: #2980b9;
                }
                
                .filters {
                    display: flex;
                    justify-content: center;
                    margin-bottom: 20px;
                    gap: 10px;
                }
                
                .filter-btn {
                    padding: 5px 10px;
                    background: none;
                    border: 1px solid #ddd;
                    border-radius: 4px;
                    cursor: pointer;
                }
                
                .filter-btn.active {
                    background: #3498db;
                    color: white;
                    border-color: #3498db;
                }
                
                #taskList {
                    list-style: none;
                    margin-bottom: 20px;
                }
                
                .task-item {
                    display: flex;
                    align-items: center;
                    padding: 10px;
                    border-bottom: 1px solid #eee;
                }
                
                .task-item:last-child {
                    border-bottom: none;
                }
                
                .task-checkbox {
                    margin-right: 10px;
                }
                
                .task-text {
                    flex: 1;
                }
                
                .task-item.completed .task-text {
                    text-decoration: line-through;
                    color: #888;
                }
                
                .delete-btn {
                    background: none;
                    border: none;
                    color: #e74c3c;
                    cursor: pointer;
                    font-size: 16px;
                }
                
                .stats {
                    display: flex;
                    justify-content: space-between;
                    align-items: center;
                    padding-top: 10px;
                    border-top: 1px solid #eee;
                    color: #777;
                    font-size: 14px;
                }
                
                #clearCompleted {
                    background: none;
                    border: none;
                    color: #777;
                    cursor: pointer;
                    text-decoration: underline;
                }
                
                @media (max-width: 480px) {
                    .input-section {
                        flex-direction: column;
                    }
                   \s
                    #taskInput, #addBtn {
                        border-radius: 4px;
                        margin-bottom: 10px;
                    }
                   \s
                    .stats {
                        flex-direction: column;
                        gap: 10px;
                    }
                }
                ```
                
                ```javascript
                // 任务管理应用
                document.addEventListener('DOMContentLoaded', function() {
                    // 获取DOM元素
                    const taskInput = document.getElementById('taskInput');
                    const addBtn = document.getElementById('addBtn');
                    const taskList = document.getElementById('taskList');
                    const itemsLeft = document.getElementById('itemsLeft');
                    const clearCompletedBtn = document.getElementById('clearCompleted');
                    const filterBtns = document.querySelectorAll('.filter-btn');
                   \s
                    // 当前过滤状态
                    let currentFilter = 'all';
                   \s
                    // 从localStorage加载任务
                    let tasks = JSON.parse(localStorage.getItem('tasks')) || [];
                   \s
                    // 初始化应用
                    function init() {
                        renderTasks();
                        updateItemsLeft();
                       \s
                        // 添加事件监听器
                        addBtn.addEventListener('click', addTask);
                        taskInput.addEventListener('keypress', function(e) {
                            if (e.key === 'Enter') addTask();
                        });
                       \s
                        clearCompletedBtn.addEventListener('click', clearCompleted);
                       \s
                        filterBtns.forEach(btn => {
                            btn.addEventListener('click', function() {
                                // 更新活动按钮状态
                                filterBtns.forEach(b => b.classList.remove('active'));
                                this.classList.add('active');
                               \s
                                // 更新过滤状态并重新渲染
                                currentFilter = this.dataset.filter;
                                renderTasks();
                            });
                        });
                    }
                   \s
                    // 添加新任务
                    function addTask() {
                        const text = taskInput.value.trim();
                        if (text === '') return;
                       \s
                        const newTask = {
                            id: Date.now(),
                            text: text,
                            completed: false
                        };
                       \s
                        tasks.push(newTask);
                        saveTasks();
                        renderTasks();
                        updateItemsLeft();
                       \s
                        // 清空输入框
                        taskInput.value = '';
                        taskInput.focus();
                    }
                   \s
                    // 渲染任务列表
                    function renderTasks() {
                        // 根据当前过滤状态筛选任务
                        let filteredTasks = tasks;
                        if (currentFilter === 'active') {
                            filteredTasks = tasks.filter(task => !task.completed);
                        } else if (currentFilter === 'completed') {
                            filteredTasks = tasks.filter(task => task.completed);
                        }
                       \s
                        // 清空列表
                        taskList.innerHTML = '';
                       \s
                        // 添加任务项
                        filteredTasks.forEach(task => {
                            const li = document.createElement('li');
                            li.className = 'task-item' + (task.completed ? ' completed' : '');
                            li.dataset.id = task.id;
                           \s
                            li.innerHTML = `
                                <input type="checkbox" class="task-checkbox" ${task.completed ? 'checked' : ''}>
                                <span class="task-text">${task.text}</span>
                                <button class="delete-btn">×</button>
                            `;
                           \s
                            taskList.appendChild(li);
                        });
                       \s
                        // 添加事件监听器
                        document.querySelectorAll('.task-checkbox').forEach(checkbox => {
                            checkbox.addEventListener('change', toggleTask);
                        });
                       \s
                        document.querySelectorAll('.delete-btn').forEach(btn => {
                            btn.addEventListener('click', deleteTask);
                        });
                    }
                   \s
                    // 切换任务完成状态
                    function toggleTask(e) {
                        const taskId = parseInt(e.target.parentElement.dataset.id);
                        const task = tasks.find(t => t.id === taskId);
                       \s
                        if (task) {
                            task.completed = !task.completed;
                            saveTasks();
                            renderTasks();
                            updateItemsLeft();
                        }
                    }
                   \s
                    // 删除任务
                    function deleteTask(e) {
                        const taskId = parseInt(e.target.parentElement.dataset.id);
                        tasks = tasks.filter(t => t.id !== taskId);
                        saveTasks();
                        renderTasks();
                        updateItemsLeft();
                    }
                   \s
                    // 清除已完成任务
                    function clearCompleted() {
                        tasks = tasks.filter(t => !t.completed);
                        saveTasks();
                        renderTasks();
                        updateItemsLeft();
                    }
                   \s
                    // 更新剩余项目数
                    function updateItemsLeft() {
                        const count = tasks.filter(t => !t.completed).length;
                        itemsLeft.textContent = `${count}个项目待完成`;
                    }
                   \s
                    // 保存任务到localStorage
                    function saveTasks() {
                        localStorage.setItem('tasks', JSON.stringify(tasks));
                    }
                   \s
                    // 初始化应用
                    init();
                });
                ```
                
                这个Todo应用具有以下特点：
                
                1. **简洁的界面设计**：专注于功能，没有多余装饰
                2. **完整的功能**：添加、完成、删除和过滤任务
                3. **数据持久化**：使用localStorage保存任务数据
                4. **响应式布局**：适配桌面和移动设备
                5. **清晰的视觉反馈**：已完成任务有删除线，不同状态有视觉区分
                
                您可以直接复制这些代码到三个对应文件中，或全部放入一个HTML文件中运行。应用会自动保存您的任务数据，即使关闭浏览器后再次打开，任务也会保留。
                """;
        MultiFileCodeResult result = CodeParser.parseMultiFileCode(codeContent);
        assertNotNull(result);
        assertNotNull(result.getHtmlCode());
        assertNotNull(result.getCssCode());
        assertNotNull(result.getJsCode());
        System.out.println(result);
    }
}
