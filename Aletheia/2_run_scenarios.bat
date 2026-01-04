@echo off
chcp 65001 > nul
echo ========================================================
echo      ALETHEIA REST API SCENARIOS RUNNER
echo ========================================================
echo.

echo [1/5] CREATE Course (Admin: Tony Stark)
echo Sending POST request...
curl -X POST http://localhost:8080/api/courses ^
     -u tony.stark:password ^
     -H "Content-Type: application/json" ^
     -d "{\"code\": \"BI-PJC\", \"name\": \"Programming in C++\", \"credits\": 6}"
echo.
echo.
echo --------------------------------------------------------
timeout /t 2 > nul

echo [2/5] READ Courses (Admin: Tony Stark)
echo Verifying creation...
curl -X GET http://localhost:8080/api/courses ^
     -u tony.stark:password
echo.
echo.
echo --------------------------------------------------------
timeout /t 2 > nul

echo [3/5] ENROLL Student (Student: Marty McFly)
echo Trying to enroll Marty (Logic Check: Duplicates)...
curl -i -X POST http://localhost:8080/api/enrollments/sections/1 ^
     -u marty.mcfly:password
echo.
echo (If you see 409 Conflict above - it is SUCCESS, logic works!)
echo.
echo --------------------------------------------------------
timeout /t 2 > nul

echo [4/5] UPDATE Capacity (Admin: Tony Stark)
echo Trying to set capacity to 0 (Logic Check: Capacity validation)...
curl -i -X PATCH "http://localhost:8080/api/courses/sections/1/capacity?capacity=0" ^
     -u tony.stark:password
echo.
echo (If you see 409 Conflict above - it is SUCCESS, logic works!)
echo.
echo --------------------------------------------------------
timeout /t 2 > nul

echo [5/5] DELETE Course (Admin: Tony Stark)
echo Deleting the course created in step 1 (ID=2)...
curl -i -X DELETE http://localhost:8080/api/courses/2 ^
     -u tony.stark:password
echo.
echo.
echo ========================================================
echo                 SCENARIOS COMPLETED
echo ========================================================
pause