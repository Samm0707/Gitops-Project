
// import React from "react";
// import { Card, CardContent } from "@/components/ui/card";
// import { Button } from "@/components/ui/button";
// import { Bell, Calendar } from "lucide-react";

// const employeeStats = [
//   { label: "Total Employees", value: 124 },
//   { label: "Present Today", value: 102 },
//   { label: "Pending Leaves", value: 5 },
//   { label: "Open Timesheets", value: 8 },
// ];

// export default function HRMSDashboard() {
//   return (
//     <div className="min-h-screen bg-[#0f1117] text-white flex">
//       {/* Sidebar */}
//       <aside className="w-64 p-6 border-r border-gray-800">
//         <div className="text-2xl font-bold mb-10">Hammer</div>
//         <nav className="space-y-4">
//           {[
//             "Dashboard",
//             "Employees",
//             "Attendance",
//             "Leave Requests",
//             "Payroll",
//             "Timesheets",
//             "Announcements",
//             "Meetings",
//           ].map((item) => (
//             <div key={item} className="text-gray-400 hover:text-white cursor-pointer">
//               {item}
//             </div>
//           ))}
//         </nav>
//       </aside>

//       {/* Main Content */}
//       <main className="flex-1 p-10">
//         <header className="mb-10">
//           <div className="text-2xl font-semibold">Welcome back, Akshay 👋</div>
//           <div className="text-sm text-gray-400 mt-1">Tuesday, April 23, 2024 • 10:45 AM</div>
//         </header>

//         {/* Stats Grid */}
//         <div className="grid grid-cols-4 gap-6 mb-10">
//           {employeeStats.map((stat) => (
//             <Card key={stat.label} className="bg-[#1c1f2b]">
//               <CardContent className="p-4">
//                 <div className="text-sm text-gray-400">{stat.label}</div>
//                 <div className="text-2xl font-semibold mt-1 text-white">
//                   {stat.value}
//                 </div>
//               </CardContent>
//             </Card>
//           ))}
//         </div>

//         {/* Check-In Card */}
//         <div className="grid grid-cols-3 gap-6 mb-10">
//           <Card className="bg-[#1c1f2b]">
//             <CardContent className="p-4">
//               <div className="text-sm text-gray-400">Check-In</div>
//               <div className="text-3xl font-bold my-2">10:45 AM</div>
//               <Button className="bg-teal-500 text-white hover:bg-teal-600 rounded-md">
//                 Check Out
//               </Button>
//             </CardContent>
//           </Card>

//           <Card className="bg-[#1c1f2b]">
//             <CardContent className="p-4">
//               <div className="text-sm text-gray-400">Anchong Teden</div>
//               <div className="mt-1">Company meeting scheduled for 3:00 PM</div>
//             </CardContent>
//           </Card>

//           <Card className="bg-[#1c1f2b]">
//             <CardContent className="p-4">
//               <div className="flex items-center justify-between mb-2">
//                 <div className="text-sm text-gray-400">Announcements</div>
//                 <Bell size={16} className="text-gray-400" />
//               </div>
//               <div>Company meeting scheduled for 3:00 PM</div>
//             </CardContent>
//           </Card>
//         </div>

//         {/* Holidays */}
//         <Card className="bg-[#1c1f2b]">
//           <CardContent className="p-4">
//             <div className="text-sm text-gray-400 mb-2">Upcoming Holidays</div>
//             <div className="flex gap-6">
//               <div>
//                 <div className="text-white font-medium">Memorial Day</div>
//                 <div className="text-gray-400 text-sm">May 27, 2024</div>
//               </div>
//               <div>
//                 <div className="text-white font-medium">Independence Day</div>
//                 <div className="text-gray-400 text-sm">July 4, 2024</div>
//               </div>
//             </div>
//           </CardContent>
//         </Card>
//       </main>
//     </div>
//   );
// }
