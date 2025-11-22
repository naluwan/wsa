/**
 * 課程詳頁（Journey Page）
 * 顯示課程的詳細資訊、章節與單元列表
 * 包含：
 * 1. 課程概覽（標題、描述、進度、XP/Level）
 * 2. 章節與單元列表（使用 Accordion 展開/收合）
 * 3. 單元項目（icon、名稱、完成狀態 badge）
 */
import Link from "next/link"
import { Video, FileText, ClipboardList, CheckCircle2, Circle, Lock } from "lucide-react"

import { Card, CardContent, CardDescription, CardHeader, CardTitle } from "@/components/ui/card"
import { Accordion, AccordionContent, AccordionItem, AccordionTrigger } from "@/components/ui/accordion"
import { Badge } from "@/components/ui/badge"
import { Progress } from "@/components/ui/progress"
import { Button } from "@/components/ui/button"

// 單元類型
type UnitType = "video" | "article" | "quiz"

// 單元資料型別
interface Unit {
  id: string
  name: string
  type: UnitType
  duration: string
  completed: boolean
  locked: boolean
  xp: number
}

// 章節資料型別
interface Episode {
  id: string
  title: string
  description: string
  units: Unit[]
}

// 課程資料型別
interface Course {
  code: string
  name: string
  description: string
  totalUnits: number
  completedUnits: number
  progress: number
  totalXp: number
  earnedXp: number
  episodes: Episode[]
}

// 根據單元類型取得對應的 icon
function getUnitIcon(type: UnitType) {
  switch (type) {
    case "video":
      return Video
    case "article":
      return FileText
    case "quiz":
      return ClipboardList
  }
}

// 根據單元類型取得對應的顏色
function getUnitTypeLabel(type: UnitType) {
  switch (type) {
    case "video":
      return "影片"
    case "article":
      return "文章"
    case "quiz":
      return "測驗"
  }
}

// 假資料：課程詳細資訊
const courseData: Record<string, Course> = {
  BACKEND_JAVA: {
    code: "BACKEND_JAVA",
    name: "後端工程師養成計畫（Java）",
    description: "從零開始學習 Spring Boot、RESTful API、資料庫設計、微服務架構，成為專業後端工程師。完整涵蓋 Java 生態系與企業級開發實戰。",
    totalUnits: 45,
    completedUnits: 12,
    progress: 27,
    totalXp: 4500,
    earnedXp: 1200,
    episodes: [
      {
        id: "ep1",
        title: "第一章：Java 基礎與環境設定",
        description: "學習 Java 語言基礎、開發環境建置、版本控制",
        units: [
          { id: "u1", name: "Java 簡介與歷史", type: "video", duration: "15 分鐘", completed: true, locked: false, xp: 100 },
          { id: "u2", name: "安裝 JDK 與 IDE", type: "video", duration: "20 分鐘", completed: true, locked: false, xp: 100 },
          { id: "u3", name: "第一個 Java 程式", type: "article", duration: "10 分鐘", completed: true, locked: false, xp: 80 },
          { id: "u4", name: "基礎語法測驗", type: "quiz", duration: "15 分鐘", completed: false, locked: false, xp: 120 },
        ],
      },
      {
        id: "ep2",
        title: "第二章：物件導向程式設計",
        description: "深入理解 OOP 概念、類別、繼承、多型、介面",
        units: [
          { id: "u5", name: "類別與物件", type: "video", duration: "25 分鐘", completed: true, locked: false, xp: 100 },
          { id: "u6", name: "封裝與存取修飾子", type: "video", duration: "20 分鐘", completed: true, locked: false, xp: 100 },
          { id: "u7", name: "繼承與多型", type: "video", duration: "30 分鐘", completed: false, locked: false, xp: 120 },
          { id: "u8", name: "介面與抽象類別", type: "article", duration: "15 分鐘", completed: false, locked: false, xp: 100 },
          { id: "u9", name: "OOP 綜合測驗", type: "quiz", duration: "20 分鐘", completed: false, locked: false, xp: 150 },
        ],
      },
      {
        id: "ep3",
        title: "第三章：Spring Boot 入門",
        description: "學習 Spring Boot 框架、依賴注入、RESTful API 開發",
        units: [
          { id: "u10", name: "Spring Boot 簡介", type: "video", duration: "20 分鐘", completed: false, locked: false, xp: 100 },
          { id: "u11", name: "建立第一個 Spring Boot 專案", type: "video", duration: "30 分鐘", completed: false, locked: false, xp: 120 },
          { id: "u12", name: "依賴注入與 IoC", type: "article", duration: "25 分鐘", completed: false, locked: false, xp: 100 },
          { id: "u13", name: "RESTful API 設計", type: "video", duration: "35 分鐘", completed: false, locked: false, xp: 150 },
          { id: "u14", name: "Spring Boot 測驗", type: "quiz", duration: "20 分鐘", completed: false, locked: true, xp: 150 },
        ],
      },
    ],
  },
  FRONTEND_REACT: {
    code: "FRONTEND_REACT",
    name: "前端工程師養成計畫（React）",
    description: "精通 React、Next.js、TypeScript，打造現代化前端應用。學習狀態管理、性能優化、響應式設計等前端核心技能。",
    totalUnits: 38,
    completedUnits: 25,
    progress: 66,
    totalXp: 3800,
    earnedXp: 2500,
    episodes: [
      {
        id: "ep1",
        title: "第一章：React 基礎",
        description: "學習 React 核心概念、JSX、組件、Props、State",
        units: [
          { id: "u1", name: "React 簡介", type: "video", duration: "15 分鐘", completed: true, locked: false, xp: 100 },
          { id: "u2", name: "建立第一個 React 應用", type: "video", duration: "20 分鐘", completed: true, locked: false, xp: 100 },
          { id: "u3", name: "JSX 語法詳解", type: "article", duration: "15 分鐘", completed: true, locked: false, xp: 80 },
        ],
      },
    ],
  },
}

export default function JourneyPage({ params }: { params: { courseCode: string } }) {
  // 取得課程資料
  const course = courseData[params.courseCode]

  // 如果找不到課程，顯示錯誤訊息
  if (!course) {
    return (
      <div className="container flex min-h-[calc(100vh-16rem)] items-center justify-center px-4">
        <Card className="w-full max-w-md">
          <CardHeader>
            <CardTitle>找不到課程</CardTitle>
            <CardDescription>您訪問的課程不存在或已被移除</CardDescription>
          </CardHeader>
          <CardContent>
            <Button asChild className="w-full">
              <Link href="/courses">返回課程列表</Link>
            </Button>
          </CardContent>
        </Card>
      </div>
    )
  }

  return (
    <div className="flex flex-col">
      {/* 課程概覽 */}
      <section className="w-full py-12 md:py-16 bg-gradient-to-b from-muted/50 to-background">
        <div className="container px-4 md:px-6">
          <div className="max-w-4xl mx-auto">
            <div className="flex flex-col md:flex-row md:items-start md:justify-between gap-6 mb-6">
              <div className="flex-1">
                <h1 className="text-3xl font-bold tracking-tighter sm:text-4xl md:text-5xl mb-3">
                  {course.name}
                </h1>
                <p className="text-muted-foreground text-lg">
                  {course.description}
                </p>
              </div>
            </div>

            {/* 進度與統計資訊 */}
            <Card>
              <CardContent className="pt-6">
                <div className="grid gap-6 md:grid-cols-3 mb-6">
                  <div className="space-y-2">
                    <p className="text-sm text-muted-foreground">完成進度</p>
                    <p className="text-2xl font-bold">{course.progress}%</p>
                    <p className="text-xs text-muted-foreground">
                      {course.completedUnits} / {course.totalUnits} 單元
                    </p>
                  </div>
                  <div className="space-y-2">
                    <p className="text-sm text-muted-foreground">獲得 XP</p>
                    <p className="text-2xl font-bold">{course.earnedXp.toLocaleString()}</p>
                    <p className="text-xs text-muted-foreground">
                      / {course.totalXp.toLocaleString()} XP
                    </p>
                  </div>
                  <div className="space-y-2">
                    <p className="text-sm text-muted-foreground">剩餘單元</p>
                    <p className="text-2xl font-bold">{course.totalUnits - course.completedUnits}</p>
                    <p className="text-xs text-muted-foreground">
                      繼續學習以完成課程
                    </p>
                  </div>
                </div>
                <Progress value={course.progress} className="h-3" />
              </CardContent>
            </Card>
          </div>
        </div>
      </section>

      {/* 章節與單元列表 */}
      <section className="w-full py-12 md:py-16">
        <div className="container px-4 md:px-6">
          <div className="max-w-4xl mx-auto">
            <h2 className="text-2xl font-bold mb-6">課程內容</h2>

            <Accordion type="multiple" defaultValue={[course.episodes[0]?.id]} className="space-y-4">
              {course.episodes.map((episode) => (
                <AccordionItem key={episode.id} value={episode.id} className="border rounded-lg px-6">
                  <AccordionTrigger className="hover:no-underline">
                    <div className="flex flex-col items-start text-left">
                      <h3 className="font-semibold text-lg">{episode.title}</h3>
                      <p className="text-sm text-muted-foreground mt-1">{episode.description}</p>
                    </div>
                  </AccordionTrigger>
                  <AccordionContent>
                    <div className="space-y-2 pt-4">
                      {episode.units.map((unit) => {
                        const Icon = getUnitIcon(unit.type)
                        return (
                          <Link
                            key={unit.id}
                            href={unit.locked ? "#" : `/units/${unit.id}`}
                            className={`flex items-center justify-between p-4 rounded-lg border transition-colors ${
                              unit.locked
                                ? "cursor-not-allowed opacity-50"
                                : "hover:bg-muted/50 cursor-pointer"
                            }`}
                            onClick={(e) => unit.locked && e.preventDefault()}
                          >
                            <div className="flex items-center gap-4 flex-1">
                              <div className="flex h-10 w-10 items-center justify-center rounded-lg bg-primary/10">
                                <Icon className="h-5 w-5 text-primary" />
                              </div>
                              <div className="flex-1">
                                <div className="flex items-center gap-2 mb-1">
                                  <p className="font-medium">{unit.name}</p>
                                  {unit.locked && <Lock className="h-4 w-4 text-muted-foreground" />}
                                </div>
                                <div className="flex items-center gap-3 text-sm text-muted-foreground">
                                  <span>{getUnitTypeLabel(unit.type)}</span>
                                  <span>•</span>
                                  <span>{unit.duration}</span>
                                  <span>•</span>
                                  <span>{unit.xp} XP</span>
                                </div>
                              </div>
                            </div>
                            <div className="flex items-center gap-2">
                              {unit.completed ? (
                                <Badge variant="secondary" className="bg-green-500/10 text-green-700 dark:text-green-400">
                                  <CheckCircle2 className="h-3 w-3 mr-1" />
                                  已完成
                                </Badge>
                              ) : unit.locked ? (
                                <Badge variant="outline">
                                  <Lock className="h-3 w-3 mr-1" />
                                  已鎖定
                                </Badge>
                              ) : (
                                <Badge variant="outline">
                                  <Circle className="h-3 w-3 mr-1" />
                                  未完成
                                </Badge>
                              )}
                            </div>
                          </Link>
                        )
                      })}
                    </div>
                  </AccordionContent>
                </AccordionItem>
              ))}
            </Accordion>
          </div>
        </div>
      </section>
    </div>
  )
}
