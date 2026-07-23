resource "grafana_folder" "xrms" {
  title = var.folder_title
}

resource "grafana_dashboard" "hrms_overview" {
  folder = grafana_folder.xrms.id
  config_json = jsonencode({
    title = "HRMS - Platform Overview"
    schemaVersion = 39
    time = { from = "now-1h", to = "now" }
    panels = [
      {
        id = 1
        title = "Request Rate (req/s)"
        type = "timeseries"
        gridPos = { h = 8, w = 12, x = 0, y = 0 }
        datasource = { type = "prometheus", uid = "prometheus" }
        targets = [{
          expr = "sum(rate(http_server_requests_seconds_count{application=\"HRMS\"}[2m]))"
          refId = "A"
        }]
      },
      {
        id = 2
        title = "Error Rate (%)"
        type = "timeseries"
        gridPos = { h = 8, w = 12, x = 12, y = 0 }
        datasource = { type = "prometheus", uid = "prometheus" }
        targets = [{
          expr = "100 * ((sum(rate(http_server_requests_seconds_count{application=\"HRMS\",status=~\"5..\"}[2m])) or vector(0)) / sum(rate(http_server_requests_seconds_count{application=\"HRMS\"}[2m])))"
          refId = "A"
        }]
      },
      {
        id = 3
        title = "HRMS Pod CPU Usage"
        type = "timeseries"
        gridPos = { h = 8, w = 12, x = 0, y = 8 }
        datasource = { type = "prometheus", uid = "prometheus" }
        targets = [{
          expr = "sum(rate(container_cpu_usage_seconds_total{namespace=\"hrms\"}[2m])) by (pod)"
          refId = "A"
        }]
      },
      {
        id = 4
        title = "Nodes by Capacity Type"
        type = "timeseries"
        gridPos = { h = 8, w = 12, x = 12, y = 8 }
        datasource = { type = "prometheus", uid = "prometheus" }
        targets = [{
          expr = "count(kube_node_labels) by (label_karpenter_sh_capacity_type)"
          refId = "A"
        }]
      },
     {
        id = 5
        title = "HRMS App Logs"
        type = "logs"
        gridPos = { h = 8, w = 24, x = 0, y = 16 }
        datasource = { type = "loki", uid = "loki" }
        targets = [{
          expr = "{namespace=\"hrms\"}"
          refId = "A"
          datasource = { type = "loki", uid = "loki" }
        }]
      }
    ]
  })
}