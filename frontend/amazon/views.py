from django.shortcuts import render
from django.http import HttpResponse

def home(request):
    return HttpResponse("<h1>Amazon Home</h1>")

def about(request):
    return HttpResponse("<h1>Amazon About</h1>")

def index(request):
    return HttpResponse("Hello, world. You're at the polls index.")