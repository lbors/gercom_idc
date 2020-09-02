#!/usr/bin/env python3
# -*- coding: utf-8 -*-
"""
Created on Wed Feb 13 18:04:22 2019

@author: billy
"""
from itertools import chain
from collections import defaultdict

import abc
import objectpath

def get_element(key):
    try:
        return key
    except KeyError:
        return False

class ProbeStrategy:
    """
    Define the interface of interest to clients.
    Maintain a reference to a Strategy object.
    """

    def __init__(self, strategy):
        self._strategy = strategy

    def select_candidates(self, agents, SlicesPartDcOrNet, partType, cost, time):
        return self._strategy.algorithm_interface(agents, SlicesPartDcOrNet, partType, cost, time)


class StrategyTypes(metaclass=abc.ABCMeta):
    """
    Declare an interface common to all supported algorithms. Context
    uses this interface to call the algorithm defined by a
    ConcreteStrategy.
    """

    @abc.abstractmethod
    def algorithm_interface(self, agents, SlicesPartDcOrNet, partType, cost, time):
        pass


class ConcreteStrategyA(StrategyTypes):
    """
    Implement the algorithm using the Strategy interface.
    """

    def algorithm_interface(self, agents, SlicesPartDcOrNet, partType, cost, time):
        print("ConcreteStrategyA")
        pass


class ConcreteStrategyB(StrategyTypes):
    """
    Implement the algorithm using the Strategy interface.
    """

    def algorithm_interface(self, agents, SlicesPartDcOrNet, partType, cost, time):
        # Simple selection of all the agents from a specific type
        # if (partType == "DC") or (partType == "WAN"):
        r = {}        
        for slice_part in SlicesPartDcOrNet:
            print("slice part ------ > ")
            print(slice_part)
            if 'location' in slice_part:
                print("Tem uma restricao geografica: ")
                for key, value in agents.items():
                    print(partType)
                    print(slice_part['location'])
                    print(value['location'])

                    if value['providerType'].find(partType.split("-")[0]) > -1 and slice_part['location'] == value['location']:
                        print('Location:',slice_part['location'])
                        if key in r:
                            slice_part.update({'cost': cost})
                            slice_part.update({'slice-time-frame':time[0]})
                            r[key].append({partType: slice_part})
                        else:
                            r[key] = []
                            slice_part.update({'cost': cost})
                            slice_part.update({'slice-time-frame':time[0]})
                            r[key].append({partType: slice_part})
                        print("VAlor do R: ",r)
            else:
                for key, value in agents.items():
                    if value['providerType'].find(partType.split("-")[0]) > -1:
                        if key in r:
                            slice_part.update({'cost': cost})
                            slice_part.update({'slice-time-frame':time[0]})
                            r[key].append({partType: slice_part})
                        else:
                            r[key] = []
                            slice_part.update({'cost': cost})
                            slice_part.update({'slice-time-frame':time[0]})
                            r[key].append({partType: slice_part})
                print("Nao tem restricao")
        print("saida da estrategia: ", r)
        return r