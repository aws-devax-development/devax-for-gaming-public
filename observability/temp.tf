

module "observability" {
  source                  = "./observability"
  adot_namespace          = "opentelemetry-operator-system"
  cluster_name            = var.cluster_name
  cluster_oidc_issuer_url = module.eks.cluster_oidc_issuer_url
  region                  = var.region
  subnet_ids              = module.vpc.private_subnets 
  security_group_ids      = [ module.eks.node_security_group_id, module.eks.cluster_primary_security_group_id ] 
  grafana_username         = var.grafana_username 
  depends_on = [
    module.eks
  ]
}

variable "grafana_username" {
  default = "yagrxu@amazon.com"
}