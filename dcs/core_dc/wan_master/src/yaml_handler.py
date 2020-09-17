import yaml


def yaml_to_dict(yaml_data):
    try:
        y = yaml.load(yaml_data, Loader=yaml.BaseLoader)
        return y
    except Exception:
        return None

def dict_to_yaml(dict_data):
    try:
        y = yaml.dump(dict_data)
        return y
    except Exception:
        return None