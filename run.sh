#!/bin/bash

# Base directory for logs and PID files
LOG_BASE="/home/administrator/CURRENT/LMS"
mkdir -p "$LOG_BASE"

# Get current timestamp for log files
TIMESTAMP=$(date +"%Y-%m-%d_%H-%M-%S")
START_TIME=$(date +"%Y-%m-%d %H:%M:%S")

# Function to start a service
start_service() {
    local service_name=$1
    local target_dir=$2
    local jar_file=$3
    local env_vars=${4:-""}  # Optional environment variables
    
    # Create log directory if it doesn't exist
    mkdir -p "$LOG_BASE/logs/$TIMESTAMP"
    
    # PID and log file paths
    local pid_file="$LOG_BASE/pids/${service_name}.pid"
    local log_file="$LOG_BASE/logs/$TIMESTAMP/${service_name}.log"
    local error_log_file="$LOG_BASE/logs/$TIMESTAMP/${service_name}_error.log"
    
    echo "[$START_TIME] Starting $service_name..."
    
    # Change directory and start the service with optional environment variables
    (cd "$target_dir" && nohup env $env_vars java -jar "$jar_file" > "$log_file" 2> "$error_log_file" & echo $! > "$pid_file")
    
    # Check if service started successfully
    local pid=$(cat "$pid_file" 2>/dev/null)
    if [ -z "$pid" ] || ! ps -p "$pid" > /dev/null; then
        echo "[$(date +'%Y-%m-%d %H:%M:%S')] ERROR: Failed to start $service_name"
        return 1
    else
        echo "[$(date +'%Y-%m-%d %H:%M:%S')] $service_name started with PID $pid"
        return 0
    fi
}

# Create directories for logs and pids
mkdir -p "$LOG_BASE/logs/$TIMESTAMP"
mkdir -p "$LOG_BASE/pids"

# Start all services
echo "=============================================="
echo "Starting all services at $START_TIME"
echo "Logs will be stored in: $LOG_BASE/logs/$TIMESTAMP"
echo "=============================================="

# Start discovery service first
start_service "discovery-service" "/home/administrator/CURRENT/SLT-Leaving-LMS/discovery-service/target" "discovery-service-0.0.1-SNAPSHOT.jar" || true

echo "Waiting 1 minute for discovery service to initialize..."
sleep 60  # Wait for 1 minute

# Start other services after the delay
start_service "api_gateway" "/home/administrator/CURRENT/SLT-Leaving-LMS/Api-Gateway/target" "Api-Gateway-0.0.1-SNAPSHOT.jar" || true
start_service "user_service" "/home/administrator/CURRENT/SLT-Leaving-LMS/User-Service/target" "UserService-LMS-1.0.jar" || true

echo "Waiting 2 minute ....."
sleep 120

# Start management service with ROSTER_BEYOND_TwentyFour=true
start_service "management_service" "/home/administrator/CURRENT/SLT-Leaving-LMS/LMS-Mangment-Service/target" "LMS-Management-Service-0.0.1-SNAPSHOT.jar" || true

start_service "roster_service" "/home/administrator/CURRENT/SLT-Leaving-LMS/Roster-Service/target" "Roster-Service-0.0.1-SNAPSHOT.jar" || true

echo "=============================================="
echo "All services started. Logs are available in:"
echo "$LOG_BASE/logs/$TIMESTAMP"
echo "Process IDs are stored in: $LOG_BASE/pids/"
echo "=============================================="
