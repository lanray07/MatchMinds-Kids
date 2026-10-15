"""App Tester endpoint. Production APKs always use /verify, never this route."""
from api.verify import handler as ProductionHandler

class handler(ProductionHandler):
    rvs_environment = 'sandbox'
