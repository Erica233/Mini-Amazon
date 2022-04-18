from django.shortcuts import render, redirect
from .forms import UserRegisterForm
from django.contrib import messages

def register(request):
    if request.method == 'POST':
        user = UserRegisterForm(request.POST)
        if user.is_valid():
            user.save()
            messages.success(request, f'Account created successfully! Now you can log in!')
            return redirect('login')
    else:
        user = UserRegisterForm()
    return render(request, 'user/register.html', {'form': user})
