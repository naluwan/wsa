"use client"

/**
 * 排行榜頁面（Leaderboard Page）
 * 顯示學習排行榜與本週成長榜
 * 包含：
 * 1. 兩個 Tab（學習排行榜、本週成長榜）
 * 2. 排名表格（排名、頭像、名稱、等級、XP）
 * 3. 前三名特殊圖示（金銀銅）
 *
 * 資料來源：後端 API /api/leaderboard/total 和 /api/leaderboard/weekly（真實資料）
 */
import { useEffect, useState } from "react"
import { Trophy, Medal, Award } from "lucide-react"

import { Tabs, TabsContent, TabsList, TabsTrigger } from "@/components/ui/tabs"
import { Card, CardContent, CardDescription, CardHeader, CardTitle } from "@/components/ui/card"
import { Table, TableBody, TableCell, TableHead, TableHeader, TableRow } from "@/components/ui/table"
import { Avatar, AvatarFallback, AvatarImage } from "@/components/ui/avatar"
import { Badge } from "@/components/ui/badge"

/**
 * 使用者排行資料型別（對應後端 LeaderboardEntryDto）
 */
interface LeaderboardUser {
  rank: number
  userId: string
  displayName: string
  avatarUrl: string | null
  level: number
  totalXp: number
  weeklyXp: number
}

// 取得排名圖示（前三名）
function getRankIcon(rank: number) {
  switch (rank) {
    case 1:
      return <Trophy className="h-6 w-6 text-yellow-500" />
    case 2:
      return <Medal className="h-6 w-6 text-gray-400" />
    case 3:
      return <Award className="h-6 w-6 text-amber-700" />
    default:
      return <span className="text-muted-foreground font-medium">{rank}</span>
  }
}

// 取得等級顏色
function getLevelColor(level: number): "default" | "secondary" | "destructive" {
  if (level >= 15) return "destructive"
  if (level >= 10) return "default"
  return "secondary"
}

/**
 * 排行榜表格組件
 */
function LeaderboardTable({ users, showWeeklyXp, loading }: {
  users: LeaderboardUser[];
  showWeeklyXp?: boolean;
  loading?: boolean;
}) {
  if (loading) {
    return (
      <div className="flex items-center justify-center py-12">
        <p className="text-muted-foreground">載入中...</p>
      </div>
    );
  }

  if (users.length === 0) {
    return (
      <div className="flex items-center justify-center py-12">
        <p className="text-muted-foreground">暫無排行資料</p>
      </div>
    );
  }

  return (
    <Table>
      <TableHeader>
        <TableRow>
          <TableHead className="w-[80px]">排名</TableHead>
          <TableHead>使用者</TableHead>
          <TableHead className="text-center">等級</TableHead>
          <TableHead className="text-right">總 XP</TableHead>
          {showWeeklyXp && <TableHead className="text-right">本週 XP</TableHead>}
        </TableRow>
      </TableHeader>
      <TableBody>
        {users.map((user) => (
          <TableRow key={user.userId}>
            {/* 排名 */}
            <TableCell className="font-medium">
              <div className="flex items-center justify-center">
                {getRankIcon(user.rank)}
              </div>
            </TableCell>

            {/* 使用者資訊 */}
            <TableCell>
              <div className="flex items-center gap-3">
                <Avatar className="h-10 w-10">
                  <AvatarImage src={user.avatarUrl || undefined} alt={user.displayName} />
                  <AvatarFallback>{user.displayName.charAt(0)}</AvatarFallback>
                </Avatar>
                <span className="font-medium">{user.displayName}</span>
              </div>
            </TableCell>

            {/* 等級 */}
            <TableCell className="text-center">
              <Badge variant={getLevelColor(user.level)}>
                Lv {user.level}
              </Badge>
            </TableCell>

            {/* 總 XP */}
            <TableCell className="text-right font-medium">
              {user.totalXp.toLocaleString()}
            </TableCell>

            {/* 本週 XP */}
            {showWeeklyXp && (
              <TableCell className="text-right font-medium text-primary">
                +{user.weeklyXp.toLocaleString()}
              </TableCell>
            )}
          </TableRow>
        ))}
      </TableBody>
    </Table>
  )
}

export default function LeaderboardPage() {
  const [totalXpLeaderboard, setTotalXpLeaderboard] = useState<LeaderboardUser[]>([]);
  const [weeklyXpLeaderboard, setWeeklyXpLeaderboard] = useState<LeaderboardUser[]>([]);
  const [loadingTotal, setLoadingTotal] = useState(true);
  const [loadingWeekly, setLoadingWeekly] = useState(true);

  // 載入總排行榜
  useEffect(() => {
    const fetchTotalLeaderboard = async () => {
      try {
        setLoadingTotal(true);
        const res = await fetch("/api/leaderboard/total");

        if (!res.ok) {
          console.error("[leaderboard/page] 取得總排行榜失敗:", res.status);
          return;
        }

        const data: LeaderboardUser[] = await res.json();
        setTotalXpLeaderboard(data);
      } catch (err) {
        console.error("[leaderboard/page] 取得總排行榜錯誤:", err);
      } finally {
        setLoadingTotal(false);
      }
    };

    fetchTotalLeaderboard();
  }, []);

  // 載入週排行榜
  useEffect(() => {
    const fetchWeeklyLeaderboard = async () => {
      try {
        setLoadingWeekly(true);
        const res = await fetch("/api/leaderboard/weekly");

        if (!res.ok) {
          console.error("[leaderboard/page] 取得週排行榜失敗:", res.status);
          return;
        }

        const data: LeaderboardUser[] = await res.json();
        setWeeklyXpLeaderboard(data);
      } catch (err) {
        console.error("[leaderboard/page] 取得週排行榜錯誤:", err);
      } finally {
        setLoadingWeekly(false);
      }
    };

    fetchWeeklyLeaderboard();
  }, []);
  return (
    <div className="flex flex-col">
      {/* 頁面標題 */}
      <section className="w-full py-12 md:py-16 bg-gradient-to-b from-muted/50 to-background">
        <div className="container px-4 md:px-6">
          <div className="flex flex-col items-center space-y-4 text-center">
            <Trophy className="h-16 w-16 text-primary" />
            <h1 className="text-3xl font-bold tracking-tighter sm:text-4xl md:text-5xl">
              排行榜
            </h1>
            <p className="mx-auto max-w-[700px] text-muted-foreground md:text-xl">
              與全站學員一同競爭，展現你的學習成果！
            </p>
          </div>
        </div>
      </section>

      {/* 排行榜內容 */}
      <section className="w-full py-12 md:py-16">
        <div className="container px-4 md:px-6">
          <Tabs defaultValue="total" className="max-w-5xl mx-auto">
            {/* Tab 選單 */}
            <TabsList className="grid w-full grid-cols-2 mb-8">
              <TabsTrigger value="total">學習排行榜</TabsTrigger>
              <TabsTrigger value="weekly">本週成長榜</TabsTrigger>
            </TabsList>

            {/* 學習排行榜（總 XP） */}
            <TabsContent value="total">
              <Card>
                <CardHeader>
                  <CardTitle>學習排行榜</CardTitle>
                  <CardDescription>
                    依照總獲得 XP 排序，展現長期學習成果
                  </CardDescription>
                </CardHeader>
                <CardContent>
                  <LeaderboardTable
                    users={totalXpLeaderboard}
                    loading={loadingTotal}
                  />
                </CardContent>
              </Card>
            </TabsContent>

            {/* 本週成長榜（本週 XP） */}
            <TabsContent value="weekly">
              <Card>
                <CardHeader>
                  <CardTitle>本週成長榜</CardTitle>
                  <CardDescription>
                    依照本週獲得 XP 排序，鼓勵持續學習
                  </CardDescription>
                </CardHeader>
                <CardContent>
                  <LeaderboardTable
                    users={weeklyXpLeaderboard}
                    showWeeklyXp
                    loading={loadingWeekly}
                  />
                </CardContent>
              </Card>
            </TabsContent>
          </Tabs>

          {/* 額外資訊 */}
          <div className="mt-8 text-center text-sm text-muted-foreground max-w-5xl mx-auto">
            <p>
              排行榜每小時更新一次。持續學習、完成單元以提升你的排名！
            </p>
          </div>
        </div>
      </section>
    </div>
  )
}
